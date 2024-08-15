package dev.mim1q.gimm1q.valuecalculators.internal;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mim1q.gimm1q.Gimm1q;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorContext;
import dev.mim1q.gimm1q.valuecalculators.variables.VariableSource;
import dev.mim1q.gimm1q.valuecalculators.variables.VariableSourceTypes;
import dev.mim1q.gimm1q.valuecalculators.variables.VariableSourceWithDependencies;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;

@ApiStatus.Internal
public class ValueCalculatorInternal {
    private final Map<String, VariableSource> variables;
    private final Map<String, WrappedExpression> expressions;
    private final ConcurrentHashMap<ValueCalculatorContext, HashMap<String, Double>> variableCache = new ConcurrentHashMap<>();

    private ValueCalculatorInternal(
        Map<String, VariableSource> variables,
        Map<String, WrappedExpression> expressions
    ) {
        this.variables = variables;
        this.expressions = expressions;
    }

    private static final Codec<WrappedExpression> EXPRESSION_BUILDER_STRING_CODEC =
        Codec.STRING.xmap(
            WrappedExpression::of,
            WrappedExpression::string
        );

    private static final Codec<WrappedExpression> EXPRESSION_BUILDER_DOUBLE_CODEC =
        Codec.DOUBLE.xmap(
            it -> WrappedExpression.of(String.valueOf(it)),
            it -> Double.valueOf(it.string)
        );

    private static final Codec<WrappedExpression> EXPRESSION_BUILDER_CODEC =
        Codec.either(EXPRESSION_BUILDER_STRING_CODEC, EXPRESSION_BUILDER_DOUBLE_CODEC).xmap(
            it -> it.map(Function.identity(), Function.identity()),
            Either::left
        );

    public static final Codec<ValueCalculatorInternal> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.unboundedMap(Codec.STRING, VariableSource.CODEC)
//            .xmap(
//                it -> List.copyOf(it.entrySet()),
//                it -> it.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
//            )
            .fieldOf("variables")
            .forGetter(it -> it.variables),
        Codec.unboundedMap(Codec.STRING, EXPRESSION_BUILDER_CODEC)
            .fieldOf("values")
            .forGetter(it -> it.expressions)
    ).apply(instance, ValueCalculatorInternal::new));

    private Optional<Double> tryCalculateVariableInternal(
        String name,
        ValueCalculatorContext context,
        HashSet<String> currentlyEvaluatedVariables
    ) {
        if (currentlyEvaluatedVariables.contains(name)) {
            Gimm1q.LOGGER.error("Value Calculator dependency cycle detected in variable: {}", name);
            return Optional.empty();
        }
        var cached = variableCache.getOrDefault(context, new HashMap<>());
        if (cached.containsKey(name)) return Optional.of(cached.get(name));

        var source = variables.get(name);
        if (source == null) return Optional.empty();

        currentlyEvaluatedVariables.add(name);
        if (source instanceof VariableSourceWithDependencies sourceWithDependencies) {
            for (var dependency : sourceWithDependencies.getPotentialVariableNames()) {
                tryCalculateVariableInternal(dependency, context, new HashSet<>(currentlyEvaluatedVariables));
            }
        }

        if (source instanceof VariableSourceTypes.Equation equationSource) {
            equationSource.setupExpressionBuilder(variableCache.get(context));
        }

        cached.put(name, source.evaluate(context));
        return Optional.ofNullable(cached.get(name));
    }

    public Optional<Double> tryCalculateVariable(String name, ValueCalculatorContext context) {
        variableCache.put(context, new HashMap<>());
        var startTime = System.nanoTime();
        var result = tryCalculateVariableInternal(name, context, new HashSet<>());
        var endTime = System.nanoTime();
        if (Gimm1q.debugMessages)
            Gimm1q.LOGGER.info("Value Calculator for variable: {} took {}ms", name, (endTime - startTime) / 1_000_000f);
        clearVariableCache(context);
        return result;
    }

    public Optional<Double> tryCalculateExpression(String name, ValueCalculatorContext context) {
        var expression = expressions.get(name);
        if (expression == null) return Optional.empty();

        variableCache.put(context, new HashMap<>());
        var startTime = System.nanoTime();
        for (var variable : expression.potentialVariables()) {
            tryCalculateVariableInternal(variable, context, new HashSet<>());
        }
        var result = Optional.of(expression.expression()
            .setVariables(variableCache.getOrDefault(context, new HashMap<>()))
            .evaluate()
        );
        var endTime = System.nanoTime();
        if (Gimm1q.debugMessages)
            Gimm1q.LOGGER.info("Value Calculator for expression: {} took {}ms", name, (endTime - startTime) / 1_000_000f);

        clearVariableCache(context);
        return result;
    }

    public Set<String> getVariableNames() {
        return variables.keySet();
    }

    public Set<String> getExpressionNames() {
        return expressions.keySet();
    }

    public void clearVariableCache(ValueCalculatorContext context) {
        variableCache.remove(context);
    }

    public record WrappedExpression(
        String string,
        Expression internalExpression,
        String[] potentialVariables
    ) {
        private static final Pattern regex = Pattern.compile("[a-z]+\\(?");

        public static WrappedExpression of(String string) {
            var potentialVariables = new HashSet<String>();
            var matcher = regex.matcher(string);
            while (matcher.find()) {
                var group = matcher.group();
                if (!group.endsWith("(")) potentialVariables.add(group);
            }

            return new WrappedExpression(
                string,
                new ExpressionBuilder(string).variables(potentialVariables).build(),
                potentialVariables.toArray(new String[0])
            );
        }

        public Expression expression() {
            return internalExpression;
        }
    }
}

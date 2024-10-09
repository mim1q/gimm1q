package dev.mim1q.gimm1q.valuecalculators.internal;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mim1q.gimm1q.Gimm1q;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorContext;
import dev.mim1q.gimm1q.valuecalculators.variables.VariableSource;
import dev.mim1q.gimm1q.valuecalculators.variables.VariableSourceWithDependencies;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.ApiStatus;
import redempt.crunch.CompiledExpression;
import redempt.crunch.Crunch;
import redempt.crunch.functional.ExpressionEnv;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@ApiStatus.Internal
public class ValueCalculatorInternal {
    @SuppressWarnings("unused")
    private static final Random RANDOM = Random.createThreadSafe();

    private final Map<String, VariableSource> variables;
    private final Map<String, Either<WrappedExpression, Double>> expressions;
    private final ConcurrentHashMap<ValueCalculatorContext, HashMap<String, Double>> variableCache = new ConcurrentHashMap<>();

    private ValueCalculatorInternal(
        Map<String, VariableSource> variables,
        Map<String, Either<WrappedExpression, Double>> expressions
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

    public static final Codec<WrappedExpression> EXPRESSION_BUILDER_CODEC =
        EXPRESSION_BUILDER_STRING_CODEC;
//        Codec.either(EXPRESSION_BUILDER_STRING_CODEC, EXPRESSION_BUILDER_DOUBLE_CODEC).xmap(
//            either -> either.map(it -> it, it -> it),
//            Either::left
//        );

    public static final Codec<ValueCalculatorInternal> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.unboundedMap(Codec.STRING, VariableSource.CODEC)
            .fieldOf("variables")
            .forGetter(it -> it.variables),
        Codec.unboundedMap(Codec.STRING, Codec.either(EXPRESSION_BUILDER_CODEC, Codec.DOUBLE))
            .fieldOf("equations")
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

//        if (source instanceof VariableSourceWithEquations equationSource) {
//            equationSource.setupExpressionBuilder(variableCache.get(context));
//        }

        cached.put(name, source.evaluate(context, cached));
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

    private Optional<Double> getDoubleValueOrCalculateExpression(String name, ValueCalculatorContext context) {
        var exp = expressions.get(name);
        if (exp == null) return Optional.empty();

        var doubleEither = exp.right();
        if (doubleEither.isPresent()) return doubleEither;

        var expressionEither = exp.left();
        if (expressionEither.isEmpty()) return Optional.empty();

        var expression = expressionEither.get();

        variableCache.put(context, new HashMap<>());
        for (var variable : expression.potentialVariables()) {
            tryCalculateVariableInternal(variable, context, new HashSet<>());
        }

        var variables = Arrays
            .stream(expression.potentialVariables)
            .mapToDouble(it -> variableCache.get(context).get(it))
            .toArray();

        return Optional.of(expression.expression()
            .evaluate(variables)
        );
    }

    public Optional<Double> tryCalculateExpression(String name, ValueCalculatorContext context) {
        var startTime = System.nanoTime();
        var result = getDoubleValueOrCalculateExpression(name, context);
        clearVariableCache(context);
        var endTime = System.nanoTime();
        if (Gimm1q.debugMessages)
            Gimm1q.LOGGER.info("Value Calculator for expression: {} took {}ms", name, (endTime - startTime) / 1_000_000f);

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
        CompiledExpression internalExpression,
        String[] potentialVariables
    ) {
        private static final Pattern REGEX = Pattern.compile("[a-z_]+\\(?");

        public static WrappedExpression of(String string) {

            var potentialVariables = new HashSet<String>();
            var matcher = REGEX.matcher(string);
            while (matcher.find()) {
                var group = matcher.group();
                if (!group.endsWith("(")) potentialVariables.add(group);
            }

            var variables = potentialVariables.stream().distinct().toArray(String[]::new);
            return new WrappedExpression(
                string,
                Crunch.compileExpression(string, new ExpressionEnv().setVariableNames(variables)),
                variables
            );
        }

        public CompiledExpression expression() {
            return internalExpression;
        }

        public double evaluate(Map<String, Double> variables) {
            return expression().evaluate(Arrays
                .stream(potentialVariables)
                .mapToDouble(it -> variables.getOrDefault(it, 0.0))
                .toArray()
            );
        }
    }
}

package dev.mim1q.gimm1q.valuecalculators.internal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mim1q.gimm1q.Gimm1q;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorContext;
import dev.mim1q.gimm1q.valuecalculators.variables.VariableSource;
import dev.mim1q.gimm1q.valuecalculators.variables.VariableSourceTypes;
import dev.mim1q.gimm1q.valuecalculators.variables.VariableSourceWithDependencies;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.*;
import java.util.regex.Pattern;

public class ValueCalculatorInternal {
    private final Map<String, VariableSource> variables;
    private final HashMap<String, Double> variableCache = new HashMap<>();
    private final Map<String, WrappedExpression> expressions;
    private final HashSet<String> currentlyEvaluatedVariables = new HashSet<>();

    private ValueCalculatorInternal(
        Map<String, VariableSource> variables,
        Map<String, WrappedExpression> expressions
    ) {
        this.variables = variables;
        this.expressions = expressions;
    }

    private static final Codec<WrappedExpression> EXPRESSION_BUILDER_CODEC =
        Codec.STRING.xmap(
            WrappedExpression::of,
            WrappedExpression::string
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

    private Optional<Double> tryCalculateVariableInternal(String name, ValueCalculatorContext context, boolean first) {
        if (first) {
            currentlyEvaluatedVariables.clear();
        }

        if (currentlyEvaluatedVariables.contains(name)) return Optional.empty();
        if (variableCache.containsKey(name)) return Optional.of(variableCache.get(name));

        var source = variables.get(name);
        if (source == null) return Optional.empty();

        if (source instanceof VariableSourceWithDependencies sourceWithDependencies) {
            for (var dependency : sourceWithDependencies.getPotentialVariableNames()) {
                tryCalculateVariableInternal(dependency, context, false);
            }
        }

        if (source instanceof VariableSourceTypes.Equation equationSource) {
            equationSource.setupExpressionBuilder(variableCache);
        }

        currentlyEvaluatedVariables.add(name);
        variableCache.put(name, source.evaluate(context));

        return Optional.ofNullable(variableCache.get(name));
    }

    public Optional<Double> tryCalculateVariable(String name, ValueCalculatorContext context) {
        clearVariableCache();
        var startTime = System.nanoTime();
        var result = tryCalculateVariableInternal(name, context, true);
        var endTime = System.nanoTime();
        if (Gimm1q.debugMessages)
            Gimm1q.LOGGER.info("Value Calculator for variable: {} took {}ms", name, (endTime - startTime) / 1_000_000f);
        return result;
    }

    public Optional<Double> tryCalculateExpression(String name, ValueCalculatorContext context) {
        clearVariableCache();
        var expression = expressions.get(name);
        var startTime = System.nanoTime();
        for (var variable : expression.potentialVariables()) {
            tryCalculateVariableInternal(variable, context, true);
        }
        var result = Optional.of(expression.expression()
            .setVariables(variableCache)
            .evaluate()
        );
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

    public void clearVariableCache() {
        variableCache.clear();
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

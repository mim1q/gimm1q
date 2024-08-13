package dev.mim1q.gimm1q.valuecalculators.internal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorContext;
import dev.mim1q.gimm1q.valuecalculators.variables.VariableSource;
import dev.mim1q.gimm1q.valuecalculators.variables.VariableSourceTypes;
import dev.mim1q.gimm1q.valuecalculators.variables.VariableSourceWithDependencies;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.*;

public class ValueCalculatorInternal {
    private final Map<String, VariableSource> variables;
    private final HashMap<String, Double> variableCache = new HashMap<>();
    private final Map<String, WrappedExpressionBuilder> expressions;
    private final Set<String> variableNames;
    private final HashSet<String> currentlyEvaluatedVariables = new HashSet<>();

    private ValueCalculatorInternal(
        Map<String, VariableSource> variables,
        Map<String, WrappedExpressionBuilder> expressions
    ) {
        this.variables = variables;
        this.expressions = expressions;
        this.variableNames = variables.keySet();
    }

    private static final Codec<WrappedExpressionBuilder> EXPRESSION_BUILDER_CODEC =
        Codec.STRING.xmap(
            WrappedExpressionBuilder::of,
            WrappedExpressionBuilder::string
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

    private Optional<Double> tryCalculateVariable(String name, ValueCalculatorContext context, boolean first) {
        if (first) {
            currentlyEvaluatedVariables.clear();
        }

        if (currentlyEvaluatedVariables.contains(name)) return Optional.empty();
        if (variableCache.containsKey(name)) return Optional.of(variableCache.get(name));

        var source = variables.get(name);
        if (source == null) return Optional.empty();

        if (source instanceof VariableSourceWithDependencies sourceWithDependencies) {
            for (var dependency : sourceWithDependencies.getPotentialVariableNames()) {
                tryCalculateVariable(dependency, context, false);
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
        return tryCalculateVariable(name, context, true);
    }

    public Optional<Double> tryCalculateExpression(String name, ValueCalculatorContext context) {
        var expression = expressions.get(name);
        variableCache.clear();
        for (var variable : expression.potentialVariables()) {
            tryCalculateVariable(variable, context, true);
        }
        return Optional.of(expression.expression()
            .variables(variableCache.keySet())
            .build()
            .setVariables(variableCache)
            .evaluate()
        );
    }

    public record WrappedExpressionBuilder(
        String string,
        ExpressionBuilder expression,
        String[] potentialVariables
    ) {
        public static WrappedExpressionBuilder of(String string) {
            return new WrappedExpressionBuilder(
                string,
                new ExpressionBuilder(string),
                string.split("\\b(?![a-z]*\\d+[a-z]*$)[^a-z]*\\b")
            );
        }
    }
}

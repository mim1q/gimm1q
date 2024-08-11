package dev.mim1q.gimm1q.valuecalculators.internal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorContext;
import dev.mim1q.gimm1q.valuecalculators.variables.VariableSource;
import dev.mim1q.gimm1q.valuecalculators.variables.VariableSourceSortUtil;
import dev.mim1q.gimm1q.valuecalculators.variables.VariableSourceTypes;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.*;
import java.util.stream.Collectors;

public class ValueCalculatorInternal {
    private final List<Map.Entry<String, VariableSource>> variables;
    private final Map<String, WrappedExpressionBuilder> expressions;
    private final Set<String> variableNames;

    private ValueCalculatorInternal(
        List<Map.Entry<String, VariableSource>> variables,
        Map<String, WrappedExpressionBuilder> expressions
    ) {
        this.variables = VariableSourceSortUtil.sortVariables(variables);
        this.expressions = expressions;
        this.variableNames = variables.stream().map(Map.Entry::getKey).collect(Collectors.toSet());
    }

    private static final Codec<WrappedExpressionBuilder> EXPRESSION_BUILDER_CODEC =
        Codec.STRING.xmap(
            string -> new WrappedExpressionBuilder(string, new ExpressionBuilder(string)),
            WrappedExpressionBuilder::string
        );

    public static final Codec<ValueCalculatorInternal> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.unboundedMap(Codec.STRING, VariableSource.CODEC)
            .xmap(
                it -> List.copyOf(it.entrySet()),
                it -> it.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            )
            .fieldOf("variables")
            .forGetter(it -> it.variables),
        Codec.unboundedMap(Codec.STRING, EXPRESSION_BUILDER_CODEC)
            .fieldOf("values")
            .forGetter(it -> it.expressions)
    ).apply(instance, ValueCalculatorInternal::new));

    public Optional<Double> tryCalculateExpression(String name, ValueCalculatorContext context) {
        var variableCache = new HashMap<String, Double>();
        for (var variable : variables) {
            var varName = variable.getKey();
            var varType = variable.getValue();

            if (varType instanceof VariableSourceTypes.Equation equationSource) {
                equationSource.setupExpressionBuilder(variableCache);
            }
            variableCache.put(varName, varType.evaluate(context));
        }

        return Optional
            .ofNullable(expressions.get(name))
            .map(it -> {
                var expression = it.expression.variables(variableNames).build();

                for (var variable : variables) {
                    var key = variable.getKey();
                    expression = expression.setVariable(key, variableCache.get(key));
                }

                return expression.evaluate();
            });
    }

    public record WrappedExpressionBuilder(
        String string,
        ExpressionBuilder expression
    ) {
    }
}

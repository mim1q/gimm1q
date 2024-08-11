package dev.mim1q.gimm1q.valuecalculators.internal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorContext;
import dev.mim1q.gimm1q.valuecalculators.variables.VariableSource;
import dev.mim1q.gimm1q.valuecalculators.variables.VariableSourceTypes;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record ValueCalculatorInternal(
    Map<String, VariableSource> variables,
    Map<String, WrappedExpressionBuilder> expressions
) {
    private static final Codec<WrappedExpressionBuilder> EXPRESSION_BUILDER_CODEC =
        Codec.STRING.xmap(
            string -> new WrappedExpressionBuilder(string, new ExpressionBuilder(string)),
            WrappedExpressionBuilder::string
        );

    public static final Codec<ValueCalculatorInternal> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.unboundedMap(Codec.STRING, VariableSource.CODEC)
            .fieldOf("variables")
            .forGetter(ValueCalculatorInternal::variables),
        Codec.unboundedMap(Codec.STRING, EXPRESSION_BUILDER_CODEC)
            .fieldOf("values")
            .forGetter(ValueCalculatorInternal::expressions)
    ).apply(instance, ValueCalculatorInternal::new));

    public Optional<Double> tryCalculateExpression(String name, ValueCalculatorContext context) {
        var variableCache = new HashMap<String, Double>();
        for (var variable : variables.entrySet()) {
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
                var expression = it.expression.variables(variables.keySet()).build();

                for (var variable : variables.entrySet()) {
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

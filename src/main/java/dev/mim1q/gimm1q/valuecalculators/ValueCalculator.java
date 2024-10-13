package dev.mim1q.gimm1q.valuecalculators;

import dev.mim1q.gimm1q.registry.ValueCalculatorResourceReloader;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorContext;
import net.minecraft.util.Identifier;

import java.util.function.Function;

/**
 * A convenient way to retrieve a value of an expression defined in the Value Calculator data.
 *
 * @param id       the identifier of the value calculator file
 * @param name     the name of the value inside the <code>values</code> map in the json file
 * @param fallback the fallback calculated if the expression is not found
 */
public record ValueCalculator(
    Identifier id,
    String name,
    Function<ValueCalculatorContext, Double> fallback
) {
    public double calculate(ValueCalculatorContext context) {
        return ValueCalculatorResourceReloader.INSTANCE.calculateExpression(id, name, context)
            .orElse(fallback.apply(context));
    }

    public double calculate() {
        return calculate(ValueCalculatorContext.create());
    }

    public static ValueCalculator of(Identifier id, String name) {
        return of(id, name, 0.0);
    }

    public static ValueCalculator of(Identifier id, String name, double fallback) {
        return new ValueCalculator(id, name, context -> fallback);
    }

    public static ValueCalculator of(Identifier id, String name, Function<ValueCalculatorContext, Double> fallback) {
        return new ValueCalculator(id, name, fallback);
    }
}

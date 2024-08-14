package dev.mim1q.gimm1q.valuecalculators;

import dev.mim1q.gimm1q.registry.ValueCalculatorResourceReloader;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorContext;
import net.minecraft.util.Identifier;

public record ValueCalculator(
    Identifier id,
    String name,
    Double fallback
) {
    public double calculate(ValueCalculatorContext context) {
        return ValueCalculatorResourceReloader.INSTANCE.calculateExpression(id, name, context).orElse(fallback);
    }

    public double calculate() {
        return calculate(ValueCalculatorContext.create());
    }

    public static ValueCalculator of(Identifier id, String name) {
        return of(id, name, 0.0);
    }

    public static ValueCalculator of(Identifier id, String name, double fallback) {
        return new ValueCalculator(id, name, fallback);
    }
}

package dev.mim1q.gimm1q.valuecalculators;

import dev.mim1q.gimm1q.registry.ValueCalculatorResourceReloader;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorContext;
import net.minecraft.util.Identifier;

public record ValueCalculator(
    Identifier id,
    String name
) {
    double calculate(ValueCalculatorContext context) {
        return ValueCalculatorResourceReloader.INSTANCE.calculate(id, name, context);
    }

    double calculate() {
        return calculate(ValueCalculatorContext.create());
    }

    public static ValueCalculator of(Identifier id, String name) {
        return new ValueCalculator(id, name);
    }
}

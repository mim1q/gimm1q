package dev.mim1q.gimm1q.valuecalculators.parameters;

import dev.mim1q.gimm1q.Gimm1q;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public record ValueCalculatorParameter<T>(
    Identifier id
) {
    public static ValueCalculatorParameter<LivingEntity> HOLDER = create(Gimm1q.id("holder"));
    public static ValueCalculatorParameter<LivingEntity> TARGET = create(Gimm1q.id("target"));
    public static ValueCalculatorParameter<ItemStack> HOLDER_STACK = create(Gimm1q.id("holder_stack"));
    public static ValueCalculatorParameter<ItemStack> TARGET_STACK = create(Gimm1q.id("target_stack"));

    public static <T> ValueCalculatorParameter<T> create(Identifier id) {
        return new ValueCalculatorParameter<>(id);
    }
}

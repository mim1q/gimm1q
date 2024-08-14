package dev.mim1q.gimm1q.valuecalculators.parameters;

import dev.mim1q.gimm1q.Gimm1q;
import dev.mim1q.gimm1q.valuecalculators.ValueCalculator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * A parameter type to be passed into a {@link ValueCalculator} through the {@link ValueCalculatorContext}
 *
 * @param id  the identifier of the parameter
 * @param <T> the type of the parameter
 */
public record ValueCalculatorParameter<T>(
    Identifier id
) {
    /**
     * Used to pass the player holding an item, the entity dealing the damage, or any other entity that may be
     * considered the "subject" of the calculation.
     */
    public static ValueCalculatorParameter<LivingEntity> HOLDER = create(Gimm1q.id("holder"));

    /**
     * Used to pass the damaged entity or another entity that may be considered the "target" or "object" of a
     * calculation.
     */
    public static ValueCalculatorParameter<LivingEntity> TARGET = create(Gimm1q.id("target"));

    /**
     * Used to pass an item stack owned by the holder that should be considered in the calculation. Useful when the
     * calculation might happen due to an item being used, but the player could already be holding a different item -
     * e.g. for ranged weapons
     */
    public static ValueCalculatorParameter<ItemStack> HOLDER_STACK = create(Gimm1q.id("holder_stack"));

    /**
     * Used to pass an item stack owned by the target that should be considered in the calculation.
     */
    public static ValueCalculatorParameter<ItemStack> TARGET_STACK = create(Gimm1q.id("target_stack"));

    /**
     * Creates a new {@link ValueCalculatorParameter}
     *
     * @param id  the identifier
     * @param <T> the type of the parameter
     * @return the new parameter type with the given identifier and type
     */
    public static <T> ValueCalculatorParameter<T> create(Identifier id) {
        return new ValueCalculatorParameter<>(id);
    }
}

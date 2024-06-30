package dev.mim1q.gimm1q.effect;

import net.minecraft.entity.effect.StatusEffect;

/**
 * Extend this interface to add additional functionality to your {@link StatusEffect}
 */
public interface ExtendedStatusEffect {

    /**
     * @return true if this effect can be cured by drinking a Milk Bucket
     */
    default boolean canBeCuredWithMilk() {
        return true;
    }
}

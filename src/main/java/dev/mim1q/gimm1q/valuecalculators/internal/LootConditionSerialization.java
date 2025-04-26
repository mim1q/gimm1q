package dev.mim1q.gimm1q.valuecalculators.internal;

import com.mojang.serialization.Codec;
import net.minecraft.loot.condition.LootCondition;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class LootConditionSerialization {
    private LootConditionSerialization() {
    }

    public static final Codec<LootCondition> CODEC = LootCondition.CODEC;
}
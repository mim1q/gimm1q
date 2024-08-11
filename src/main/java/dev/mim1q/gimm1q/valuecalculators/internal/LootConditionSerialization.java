package dev.mim1q.gimm1q.valuecalculators.internal;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.condition.LootCondition;

public final class LootConditionSerialization {
    private LootConditionSerialization() {
    }

    private static final Gson GSON = LootDataType.PREDICATES.getGson();

    public static final Codec<LootCondition> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<T> encode(LootCondition input, DynamicOps<T> ops, T prefix) {
            return DataResult.success(
                JsonOps.INSTANCE.convertTo(
                    ops,
                    GSON.toJsonTree(input, LootCondition.class)
                )
            );
        }

        @Override
        public <T> DataResult<Pair<LootCondition, T>> decode(DynamicOps<T> ops, T input) {
            return DataResult.success(Pair.of(
                GSON.fromJson(
                    ops.convertTo(JsonOps.INSTANCE, input),
                    LootCondition.class
                ),
                input
            ));
        }
    };
}
package dev.mim1q.gimm1q.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class Gimm1qCodecUtil {
    public static <T> Codec<T> createIdentifierToRegistryCodec(Registry<T> registry) {
        return Identifier.CODEC.flatXmap(
            id -> {
                try {
                    var value = registry.get(id);
                    if (value == null) {
                        return DataResult.error(() -> "Identifier " + id + " not found in " + registry.getKey().getValue());
                    }
                    return DataResult.success(value);
                } catch (Exception ignored) {
                    return DataResult.error(() -> "Error while getting " + id + " from " + registry.getKey().getValue());
                }
            },
            value -> {
                var id = registry.getId(value);
                return id == null
                    ? DataResult.error(() -> "Identifier " + value + " not found in " + registry.getKey().getValue())
                    : DataResult.success(id);
            }
        );
    }
}

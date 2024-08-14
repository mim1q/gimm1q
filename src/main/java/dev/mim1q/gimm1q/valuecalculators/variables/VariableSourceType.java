package dev.mim1q.gimm1q.valuecalculators.variables;

import com.mojang.serialization.Codec;

/**
 * The type of a given {@link VariableSource} subtype
 *
 * @param codec the codec to serialize and deserialize the {@link VariableSource}
 * @param <T>   the subtype of the {@link VariableSource}
 */
public record VariableSourceType<T extends VariableSource>(
    Codec<T> codec
) {
}

package dev.mim1q.gimm1q.valuecalculators.variables;

import com.mojang.serialization.Codec;

public record VariableSourceType<T extends VariableSource>(
    Codec<T> codec
) {
}

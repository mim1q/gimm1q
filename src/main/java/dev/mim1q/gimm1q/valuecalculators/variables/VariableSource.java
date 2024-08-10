package dev.mim1q.gimm1q.valuecalculators.variables;

import com.mojang.serialization.Codec;
import dev.mim1q.gimm1q.registry.Gimm1qRegistries;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorContext;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorParameter;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

public interface VariableSource {
    Codec<VariableSource> CODEC = Gimm1qRegistries.VARIABLE_SOURCE_TYPE.getCodec()
        .dispatch("type", VariableSource::getType, VariableSourceType::codec);

    @ApiStatus.OverrideOnly
    double evaluate(ValueCalculatorContext context);

    @ApiStatus.NonExtendable
    default double evaluate() {
        return evaluate(ValueCalculatorContext.create());
    }

    VariableSourceType<? extends VariableSource> getType();

    default List<ValueCalculatorParameter<?>> getRequiredParameters() {
        return List.of();
    }

    static <T extends VariableSource> VariableSourceType<T> register(Identifier id, Codec<T> codec) {
        return Registry.register(
            Gimm1qRegistries.VARIABLE_SOURCE_TYPE,
            id,
            new VariableSourceType<>(codec)
        );
    }
}

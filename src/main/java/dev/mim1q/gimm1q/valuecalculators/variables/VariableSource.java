package dev.mim1q.gimm1q.valuecalculators.variables;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.mim1q.gimm1q.registry.Gimm1qRegistries;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorContext;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorParameter;
import dev.mim1q.gimm1q.valuecalculators.variables.VariableSourceTypes.Constant;
import dev.mim1q.gimm1q.valuecalculators.variables.VariableSourceTypes.Equation;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

public interface VariableSource {
    Codec<VariableSource> TYPED_CODEC = Gimm1qRegistries.VARIABLE_SOURCE_TYPE.getCodec()
        .dispatch("type", VariableSource::getType, VariableSourceType::codec);

    Codec<VariableSource> CONSTANT_CODEC = Codec.DOUBLE.xmap(Constant::new, it -> ((Constant) it).value());

    Codec<VariableSource> EQUATION_CODEC = Codec.STRING.xmap(Equation::new, it -> ((Equation) it).expressionString);

    Codec<VariableSource> CONSTANT_OR_EQUATION_CODEC =
        Codec.either(CONSTANT_CODEC, EQUATION_CODEC).xmap(
            either -> either.map(it -> it, it -> it),
            Either::left
        );

    Codec<VariableSource> CODEC =
        Codec.either(CONSTANT_OR_EQUATION_CODEC, TYPED_CODEC).xmap(
            either -> either.map(it -> it, it -> it),
            Either::left
        );

    @ApiStatus.OverrideOnly
    double evaluate(ValueCalculatorContext context);

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

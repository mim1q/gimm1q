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

/**
 * Interface for implementing custom sources for variables in value calculators.
 * Examples of custom sources can be found in {@link VariableSourceTypes}
 */
public interface VariableSource {
    /**
     * Internal codec used for serializing and deserializing {@link VariableSource}s with provided {@code type}s
     */
    Codec<VariableSource> TYPED_CODEC = Gimm1qRegistries.VARIABLE_SOURCE_TYPE.getCodec()
        .dispatch("type", VariableSource::getType, VariableSourceType::codec);

    /**
     * Internal codec used for serializing and deserializing a {@link VariableSourceTypes.Constant} based on a
     * {@code double}
     */
    Codec<VariableSource> CONSTANT_CODEC = Codec.DOUBLE.xmap(Constant::new, it -> ((Constant) it).value());

    /**
     * Internal codec used for serializing and deserializing a {@link VariableSourceTypes.Equation} based on the
     * equation's {@code String} representation
     */
    Codec<VariableSource> EQUATION_CODEC = Codec.STRING.xmap(Equation::new, it -> ((Equation) it).getExpressionString());

    /**
     * Internal codec used for serializing and deserializing a {@link VariableSourceTypes.Constant} or
     * {@link VariableSourceTypes.Equation} based on the provided {@code String} or {@code double}.
     */
    Codec<VariableSource> CONSTANT_OR_EQUATION_CODEC =
        Codec.either(CONSTANT_CODEC, EQUATION_CODEC).xmap(
            either -> either.map(it -> it, it -> it),
            it -> {
                if (it instanceof Constant) {
                    return Either.left(it);
                }
                return Either.right(it);
            }
        );

    /**
     * Codec used for serializing a {@link VariableSource} if an appropriate object with the type is provided,
     * or a {@link VariableSourceTypes.Constant} or {@link VariableSourceTypes.Equation} if a {@code double} or
     * {@code String}, respectively, is provided
     */
    Codec<VariableSource> CODEC =
        Codec.either(CONSTANT_OR_EQUATION_CODEC, TYPED_CODEC).xmap(
            either -> either.map(it -> it, it -> it),
            it -> {
                if (it instanceof Constant || it instanceof Equation) {
                    return Either.left(it);
                }
                return Either.right(it);
            }
        );

    /**
     * Returns the value of this {@link VariableSource} evaluated with the parameters provided through the
     * {@link ValueCalculatorContext}
     *
     * @param context the context with the provided parameters
     * @return the evaluated value
     */
    @ApiStatus.OverrideOnly
    double evaluate(ValueCalculatorContext context);

    /**
     * Gets the registered type of this {@link VariableSource}
     *
     * @return the type of this {@link VariableSource}
     */
    VariableSourceType<? extends VariableSource> getType();

    /**
     * Returns the required parameters for this {@link VariableSource} to be evaluated correctly
     *
     * @return the required parameters
     */
    default List<ValueCalculatorParameter<?>> getRequiredParameters() {
        return List.of();
    }

    /**
     * Registers a new {@link VariableSourceType}
     *
     * @param id    the identifier of the registered type
     * @param codec the codec to serialize and deserialize the {@link VariableSource}
     * @param <T>   the subtype of the {@link VariableSource}
     * @return the registered type
     */
    static <T extends VariableSource> VariableSourceType<T> register(Identifier id, Codec<T> codec) {
        return Registry.register(
            Gimm1qRegistries.VARIABLE_SOURCE_TYPE,
            id,
            new VariableSourceType<>(codec)
        );
    }
}

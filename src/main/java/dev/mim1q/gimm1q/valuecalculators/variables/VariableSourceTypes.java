package dev.mim1q.gimm1q.valuecalculators.variables;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mim1q.gimm1q.Gimm1q;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorContext;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorParameter;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.util.StringIdentifiable;

import java.util.List;

public final class VariableSourceTypes {
    public static final VariableSourceType<?> CONSTANT =
        VariableSource.register(Gimm1q.id("constant"), Constant.CODEC);

    public static final VariableSourceType<?> ATTRIBUTE =
        VariableSource.register(Gimm1q.id("attribute"), Attribute.CODEC);

    public static void init() {
    }

    private record Constant(
        double value
    ) implements VariableSource {
        public static final Codec<Constant> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("value").forGetter(Constant::value)
        ).apply(instance, Constant::new));

        @Override
        public double evaluate(ValueCalculatorContext context) {
            return value;
        }

        @Override
        public VariableSourceType<? extends VariableSource> getType() {
            return CONSTANT;
        }
    }

    private record Attribute(
        EntityAttribute attribute,
        EntitySelector selector,
        double fallback
    ) implements VariableSource {
        public static final Codec<Attribute> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registries.ATTRIBUTE.getCodec()
                .fieldOf("attribute")
                .forGetter(Attribute::attribute),
            StringIdentifiable.createCodec(EntitySelector::values)
                .optionalFieldOf("selector", EntitySelector.THIS)
                .forGetter(Attribute::selector),
            Codec.DOUBLE
                .optionalFieldOf("fallback", 0.0)
                .forGetter(Attribute::fallback)
        ).apply(instance, Attribute::new));

        @Override
        public double evaluate(ValueCalculatorContext context) {
            var parameter = selector == EntitySelector.THIS
                ? ValueCalculatorParameter.HOLDER
                : ValueCalculatorParameter.TARGET;

            return context.mapOrDefault(
                parameter,
                value -> value.getAttributes().hasAttribute(attribute)
                    ? value.getAttributeValue(attribute)
                    : fallback,
                0.0
            );
        }

        @Override
        public VariableSourceType<? extends VariableSource> getType() {
            return ATTRIBUTE;
        }

        @Override
        public List<ValueCalculatorParameter<?>> getRequiredParameters() {
            if (selector == EntitySelector.THIS) {
                return List.of(ValueCalculatorParameter.HOLDER);
            } else {
                return List.of(ValueCalculatorParameter.TARGET);
            }
        }
    }

    public enum EntitySelector implements StringIdentifiable {
        THIS("this"),
        TARGET("target");

        private final String name;

        EntitySelector(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return name;
        }
    }
}

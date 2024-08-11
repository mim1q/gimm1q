package dev.mim1q.gimm1q.valuecalculators.variables;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mim1q.gimm1q.Gimm1q;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorContext;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorParameter;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.StringIdentifiable;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.List;
import java.util.Map;

public final class VariableSourceTypes {
    public static final VariableSourceType<?> CONSTANT =
        VariableSource.register(Gimm1q.id("constant"), Constant.CODEC);

    public static final VariableSourceType<?> EQUATION =
        VariableSource.register(Gimm1q.id("equation"), Equation.CODEC);

    public static final VariableSourceType<?> ATTRIBUTE =
        VariableSource.register(Gimm1q.id("attribute"), Attribute.CODEC);

    public static final VariableSourceType<?> ENCHANTMENT =
        VariableSource.register(Gimm1q.id("enchantment"), Enchantment.CODEC);

    public static void init() {
    }

    public record Constant(
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

    public static class Equation implements VariableSource {
        public final String expressionString;
        private boolean setup = false;
        private final ExpressionBuilder expressionBuilder;
        private Expression currentExpression = null;

        Equation(String expression) {
            this.expressionString = expression;
            this.expressionBuilder = new ExpressionBuilder(expressionString);
        }

        public void setupExpressionBuilder(Map<String, Double> previousVariables) {
            if (!setup) {
                setup = true;
                expressionBuilder.variables(previousVariables.keySet());
            }

            currentExpression = expressionBuilder.build();
            currentExpression.setVariables(previousVariables);
        }

        @Override
        public double evaluate(ValueCalculatorContext context) {
            return currentExpression == null
                ? 0.0
                : currentExpression.evaluate();
        }

        @Override
        public VariableSourceType<? extends VariableSource> getType() {
            return EQUATION;
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
            var parameter = selector.parameter;

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
            return List.of(selector.parameter);
        }
    }

    private record Enchantment(
        net.minecraft.enchantment.Enchantment enchantment
    ) implements VariableSource {
        public static final Codec<Enchantment> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registries.ENCHANTMENT.getCodec()
                .fieldOf("enchantment")
                .forGetter(Enchantment::enchantment)
        ).apply(instance, Enchantment::new));

        @Override
        public double evaluate(ValueCalculatorContext context) {
            var stack = context.mapOrDefault(
                ValueCalculatorParameter.HOLDER_STACK,
                it -> it,
                context.mapOrDefault(
                    ValueCalculatorParameter.HOLDER,
                    it -> it.getStackInHand(it.getActiveHand()),
                    ItemStack.EMPTY
                )
            );

            return EnchantmentHelper.getLevel(enchantment, stack);
        }

        @Override
        public List<ValueCalculatorParameter<?>> getRequiredParameters() {
            return List.of(ValueCalculatorParameter.HOLDER);
        }

        @Override
        public VariableSourceType<? extends VariableSource> getType() {
            return ENCHANTMENT;
        }
    }

    public enum EntitySelector implements StringIdentifiable {
        THIS("this", ValueCalculatorParameter.HOLDER, ValueCalculatorParameter.HOLDER_STACK),
        TARGET("target", ValueCalculatorParameter.TARGET, ValueCalculatorParameter.TARGET_STACK);

        private final String name;
        public final ValueCalculatorParameter<LivingEntity> parameter;
        public final ValueCalculatorParameter<ItemStack> itemParameter;

        EntitySelector(
            String name,
            ValueCalculatorParameter<LivingEntity> parameter,
            ValueCalculatorParameter<ItemStack> itemParameter
        ) {
            this.name = name;
            this.parameter = parameter;
            this.itemParameter = itemParameter;
        }

        @Override
        public String asString() {
            return name;
        }
    }
}

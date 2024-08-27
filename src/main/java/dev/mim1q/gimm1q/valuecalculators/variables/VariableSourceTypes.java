package dev.mim1q.gimm1q.valuecalculators.variables;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mim1q.gimm1q.Gimm1q;
import dev.mim1q.gimm1q.valuecalculators.internal.LootConditionSerialization;
import dev.mim1q.gimm1q.valuecalculators.internal.ValueCalculatorInternal.WrappedExpression;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorContext;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorParameter;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.StringIdentifiable;
import net.objecthunter.exp4j.Expression;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

/**
 * This class contains some basic types of {@link VariableSource}s with examples on how to implement them
 */
public final class VariableSourceTypes {
    /**
     * Always returns the same provided value
     * <pre>{@code
     *     "variables": {
     *         "my_var": 10.0
     *     }
     * }</pre>
     */
    public static final VariableSourceType<?> CONSTANT =
        VariableSource.register(Gimm1q.id("constant"), Constant.CODEC);

    /**
     * Returns a value based on an equation. It may use other variables, but you must make sure that there are no
     * cyclic dependencies between different equations
     *
     * <pre>{@code
     *     "variables": {
     *         "a": 10.0,
     *         "b": 20.0,
     *         "my_var": "a + b"
     *     }
     * }</pre>
     */
    public static final VariableSourceType<?> EQUATION =
        VariableSource.register(Gimm1q.id("equation"), Equation.CODEC);

    /**
     * Returns a value based on an entity's attribute value
     *
     * <pre>{@code
     *     "variables": {
     *         "my_var": {
     *             "type": "gimm1q:attribute",
     *             "attribute": "generic.attack_damage",
     *             "fallback": 10.0
     *         }
     *     }
     * }</pre>
     */
    public static final VariableSourceType<?> ATTRIBUTE =
        VariableSource.register(Gimm1q.id("attribute"), Attribute.CODEC);

    /**
     * Returns the level of the provided enchantment (0 if the enchantment is not present).
     * The value is based on an entity's held item or the context item stack (if provided)
     *
     * <pre>{@code
     *     "variables": {
     *         "my_var": {
     *             "type": "gimm1q:enchantment",
     *             "enchantment": "minecraft:knockback",
     *         }
     *     }
     * }</pre>
     */
    public static final VariableSourceType<?> ENCHANTMENT =
        VariableSource.register(Gimm1q.id("enchantment"), Enchantment.CODEC);

    /**
     * Returns values based on a condition. The provided condition syntax is a {@link LootCondition}.
     * A good resource for creating these is <a href="https://misode.github.io/loot-table/">Misode's loot table
     * generator</a>, when adding a new condition to a Loot Table.
     *
     * <p>
     * Both if_true and if_false can be any other Variable Source type. In this case, they're constants
     * </p>
     *
     * <pre>{@code
     *     "variables": {
     *         "my_var": {
     *             "type": "gimm1q:condition",
     *             "condition": {
     *                 "condition": "minecraft:random_chance",
     *                 "chance": 0.5
     *             },
     *             "if_true": 10.0,
     *             "if_false": 5.0
     *         }
     *     }
     * }</pre>
     */
    public static final VariableSourceType<?> CONDITION =
        VariableSource.register(Gimm1q.id("condition"), Condition.CODEC);

    /**
     * Similar to {@link #CONDITION}, but can have multiple values that work similar to a switch statement.
     * The first condition that resolves to true will have its value returned. If no condition matches, it will use
     * its provided fallback value.
     *
     * <p>
     * Both the results and the fallback can be any other Variable Source type. In this case, they're constants.
     * </p>
     *
     * <pre>{@code
     *     "variables": {
     *         "my_var": {
     *             "type": "gimm1q:switch",
     *             "cases": [
     *                 {
     *                     "condition": {
     *                         "condition": "minecraft:weather_check",
     *                         "raining": true
     *                     },
     *                     "result": 10.0
     *                 },
     *                 {
     *                     "condition": {
     *                         "condition": "minecraft:random_chance",
     *                         "chance": 0.5
     *                     },
     *                     "result": 5.0
     *                 }
     *             ],
     *             "fallback": 0.0
     *         }
     *     }
     * }</pre>
     */
    public static final VariableSourceType<?> SWITCH =
        VariableSource.register(Gimm1q.id("switch"), Switch.CODEC);

    /**
     * Returns a value based on defined thresholds of a given value.
     * The thresholds should be ordered from lowest to highest. For example:
     *
     * <pre>{@code
     *     "variables": {
     *         "my_var": {
     *             "type": "gimm1q:thresholds",
     *             "value": "some_other_variable",
     *             "thresholds": [
     *                  {
     *                      "threshold": 0.0,
     *                      "result": 10.0
     *                  },
     *                  {
     *                      "threshold": 0.5,
     *                      "result": 5.0
     *                  },
     *                  {
     *                      "threshold": 1.0,
     *                      "result": 0.0
     *                  }
     *             ]
     *             "fallback": 10.0
     *         }
     *     }
     * }</pre>
     */
    public static final VariableSourceType<?> THRESHOLDS =
        VariableSource.register(Gimm1q.id("thresholds"), Thresholds.CODEC);

    public static void init() {
    }

    public record Constant(
        double value
    ) implements VariableSource {
        public static final Constant ZERO = new Constant(0.0);
        public static final Constant ONE = new Constant(1.0);

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

    public static class Equation implements VariableSourceWithDependencies {
        private final WrappedExpression expressionBuilder;
        private Expression currentExpression = null;

        Equation(String expression) {
            this.expressionBuilder = WrappedExpression.of(expression);
        }

        public void setupExpressionBuilder(Map<String, Double> previousVariables) {
            currentExpression = expressionBuilder.expression();
            currentExpression.setVariables(previousVariables);
        }

        public String getExpressionString() {
            return expressionBuilder.string();
        }

        @Override
        public String[] getPotentialVariableNames() {
            return expressionBuilder.potentialVariables();
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

    private record Condition(
        LootCondition condition,
        VariableSource ifTrue,
        VariableSource ifFalse
    ) implements VariableSourceWithDependencies {
        public static final Codec<Condition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LootConditionSerialization.CODEC
                .fieldOf("condition")
                .forGetter(Condition::condition),
            VariableSource.CODEC
                .optionalFieldOf("if_true", Constant.ONE)
                .forGetter(Condition::ifTrue),
            VariableSource.CODEC
                .optionalFieldOf("if_false", Constant.ZERO)
                .forGetter(Condition::ifFalse)
        ).apply(instance, Condition::new));


        @Override
        public double evaluate(ValueCalculatorContext context) {
            var lootContext = createLootContext(context);
            if (lootContext == null) return 0.0;

            return condition.test(lootContext)
                ? ifTrue.evaluate(context)
                : ifFalse.evaluate(context);
        }

        @Override
        public VariableSourceType<? extends VariableSource> getType() {
            return CONDITION;
        }

        @Override
        public List<ValueCalculatorParameter<?>> getRequiredParameters() {
            var result = new ArrayList<ValueCalculatorParameter<?>>();
            result.addAll(ifTrue.getRequiredParameters());
            result.addAll(ifFalse.getRequiredParameters());

            return result;
        }

        @Override
        public String[] getPotentialVariableNames() {
            var ifTrueDependencies = (ifTrue instanceof VariableSourceWithDependencies sourceWithDependencies)
                ? sourceWithDependencies.getPotentialVariableNames() : new String[0];
            var ifFalseDependencies = (ifFalse instanceof VariableSourceWithDependencies sourceWithDependencies)
                ? sourceWithDependencies.getPotentialVariableNames() : new String[0];

            return Stream
                .concat(Arrays.stream(ifTrueDependencies), Arrays.stream(ifFalseDependencies))
                .distinct()
                .toArray(String[]::new);
        }

        static @Nullable LootContext createLootContext(ValueCalculatorContext context) {
            var holder = context.get(ValueCalculatorParameter.HOLDER);
            if (holder == null || holder.getWorld().isClient()) {
                return null;
            }

            return new LootContext.Builder(
                new LootContextParameterSet(
                    (ServerWorld) holder.getWorld(),
                    Map.of(
                        LootContextParameters.THIS_ENTITY,
                        Objects.requireNonNull(context.get(ValueCalculatorParameter.HOLDER))
                    ),
                    Map.of(),
                    (holder instanceof PlayerEntity player) ? player.getLuck() : 0f
                )
            ).build(null);
        }
    }

    public record Switch(
        List<Case> cases,
        VariableSource fallback
    ) implements VariableSourceWithDependencies {
        public static final Codec<Switch> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec
                .list(Case.CODEC)
                .fieldOf("cases")
                .forGetter(Switch::cases),
            VariableSource.CODEC
                .fieldOf("fallback")
                .forGetter(Switch::fallback)
        ).apply(instance, Switch::new));

        @Override
        public double evaluate(ValueCalculatorContext context) {
            var lootContext = Condition.createLootContext(context);
            for (var currentCase : cases) {
                if (currentCase.condition.test(lootContext)) {
                    return currentCase.result.evaluate(context);
                }
            }

            return fallback.evaluate(context);
        }

        @Override
        public VariableSourceType<? extends VariableSource> getType() {
            return SWITCH;
        }

        @Override
        public String[] getPotentialVariableNames() {
            Stream<String> stream = Stream.of();
            for (var currentCase : cases) {
                if (currentCase.condition instanceof VariableSourceWithDependencies sourceWithDependencies) {
                    stream = Stream.concat(stream, Arrays.stream(sourceWithDependencies.getPotentialVariableNames()));
                }
            }

            return stream
                .distinct()
                .toArray(String[]::new);
        }

        private record Case(
            LootCondition condition,
            VariableSource result
        ) {
            public static final Codec<Case> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LootConditionSerialization.CODEC
                    .fieldOf("condition")
                    .forGetter(Case::condition),
                VariableSource.CODEC
                    .fieldOf("result")
                    .forGetter(Case::result)
            ).apply(instance, Case::new));
        }
    }

    public record Thresholds(
        VariableSource value,
        List<Threshold> thresholds,
        VariableSource fallback
    ) implements VariableSourceWithDependencies {
        public static final Codec<Thresholds> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            VariableSource.CODEC
                .fieldOf("value")
                .forGetter(Thresholds::value),
            Codec
                .list(Threshold.CODEC)
                .fieldOf("thresholds")
                .forGetter(Thresholds::thresholds),
            VariableSource.CODEC
                .fieldOf("fallback")
                .forGetter(Thresholds::fallback)
        ).apply(instance, Thresholds::new));

        @Override
        public double evaluate(ValueCalculatorContext context) {
            var value = this.value.evaluate(context);
            for (var currentThreshold : thresholds) {
                if (value >= currentThreshold.threshold) {
                    return currentThreshold.result.evaluate(context);
                }
            }

            return fallback.evaluate(context);
        }

        @Override
        public VariableSourceType<? extends VariableSource> getType() {
            return THRESHOLDS;
        }

        @Override
        public String[] getPotentialVariableNames() {
            Stream<String> stream = Stream.of();
            for (var currentThreshold : thresholds) {
                if (currentThreshold.result instanceof VariableSourceWithDependencies sourceWithDependencies) {
                    stream = Stream.concat(stream, Arrays.stream(sourceWithDependencies.getPotentialVariableNames()));
                }
            }
            if (value instanceof VariableSourceWithDependencies sourceWithDependencies) {
                stream = Stream.concat(stream, Arrays.stream(sourceWithDependencies.getPotentialVariableNames()));
            }
            return stream.distinct().toArray(String[]::new);
        }

        @Override
        public List<ValueCalculatorParameter<?>> getRequiredParameters() {
            Stream<ValueCalculatorParameter<?>> stream = value.getRequiredParameters().stream();
            for (var currentThreshold : thresholds) {
                stream = Stream.concat(stream, currentThreshold.result.getRequiredParameters().stream());
            }

            return stream.toList();
        }

        private record Threshold(
            double threshold,
            VariableSource result
        ) {
            public static final Codec<Threshold> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("threshold")
                    .forGetter(Threshold::threshold),
                VariableSource.CODEC
                    .fieldOf("result")
                    .forGetter(Threshold::result)
            ).apply(instance, Threshold::new));
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

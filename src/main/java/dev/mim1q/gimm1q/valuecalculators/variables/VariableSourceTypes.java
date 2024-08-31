package dev.mim1q.gimm1q.valuecalculators.variables;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mim1q.gimm1q.Gimm1q;
import dev.mim1q.gimm1q.valuecalculators.internal.LootConditionSerialization;
import dev.mim1q.gimm1q.valuecalculators.internal.ValueCalculatorInternal;
import dev.mim1q.gimm1q.valuecalculators.internal.ValueCalculatorInternal.WrappedExpression;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorContext;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorParameter;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.StringIdentifiable;
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
     *             ],
     *             "fallback": 10.0
     *         }
     *     }
     * }</pre>
     */
    public static final VariableSourceType<?> THRESHOLDS =
        VariableSource.register(Gimm1q.id("thresholds"), Thresholds.CODEC);

    public static final VariableSourceType<?> ENTITY_NBT =
        VariableSource.register(Gimm1q.id("entity_nbt"), NbtVariableSource.createCodec(EntityNbtVariableSource::new));

    public static final VariableSourceType<?> ITEM_NBT =
        VariableSource.register(Gimm1q.id("item_nbt"), NbtVariableSource.createCodec(ItemNbtVariableSource::new));

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
        public double evaluate(ValueCalculatorContext context, Map<String, Double> previousVariables) {
            return value;
        }

        @Override
        public VariableSourceType<? extends VariableSource> getType() {
            return CONSTANT;
        }
    }

    public static class Equation implements VariableSourceWithEquations {
        private final WrappedExpression expressionBuilder;

        Equation(String expression) {
            this.expressionBuilder = WrappedExpression.of(expression);
        }

        public String getExpressionString() {
            return expressionBuilder.string();
        }

        @Override
        public String[] getPotentialVariableNames() {
            return expressionBuilder.potentialVariables();
        }

        @Override
        public double evaluate(ValueCalculatorContext context, Map<String, Double> previousVariables) {
            return expressionBuilder.evaluate(previousVariables);
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
        public double evaluate(ValueCalculatorContext context, Map<String, Double> previousVariables) {
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
        public double evaluate(ValueCalculatorContext context, Map<String, Double> previousVariables) {
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
        Either<LootCondition, WrappedExpression> condition,
        VariableSource ifTrue,
        VariableSource ifFalse
    ) implements VariableSourceWithEquations {
        static final Codec<Either<LootCondition, WrappedExpression>> CONDITION_CODEC = Codec.either(
            LootConditionSerialization.CODEC,
            ValueCalculatorInternal.EXPRESSION_BUILDER_CODEC
        );

        public static final Codec<Condition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CONDITION_CODEC
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
        public double evaluate(ValueCalculatorContext context, Map<String, Double> previousVariables) {
            return testCondition(condition, context, previousVariables)
                ? ifTrue.evaluate(context, previousVariables)
                : ifFalse.evaluate(context, previousVariables);
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
            Set<String> result = new HashSet<>();

            if (ifTrue instanceof VariableSourceWithDependencies sourceWithDependencies) {
                Collections.addAll(result, sourceWithDependencies.getPotentialVariableNames());
            }

            if (ifFalse instanceof VariableSourceWithDependencies sourceWithDependencies) {
                Collections.addAll(result, sourceWithDependencies.getPotentialVariableNames());
            }

            if (condition.right().isPresent()) {
                Collections.addAll(result, condition.right().get().potentialVariables());
            }

            return result.toArray(new String[0]);
        }

        static boolean testCondition(Either<LootCondition, WrappedExpression> condition, ValueCalculatorContext context, Map<String, Double> previousVariables) {
            if (condition.left().isPresent()) {
                var lootContext = createLootContext(context, previousVariables);
                if (lootContext == null) return false;
                return condition.left().get().test(lootContext);
            } else {
                return condition.right().orElseThrow().evaluate(previousVariables) != 0.0;
            }
        }

        static @Nullable LootContext createLootContext(ValueCalculatorContext context, Map<String, Double> previousVariables) {
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
    ) implements VariableSourceWithEquations {
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
        public double evaluate(ValueCalculatorContext context, Map<String, Double> previousVariables) {
            for (var currentCase : cases) {
                if (Condition.testCondition(currentCase.condition, context, previousVariables)) {
                    return currentCase.result.evaluate(context, previousVariables);
                }
            }

            return fallback.evaluate(context, previousVariables);
        }

        @Override
        public VariableSourceType<? extends VariableSource> getType() {
            return SWITCH;
        }

        @Override
        public String[] getPotentialVariableNames() {
            Stream<String> stream = Stream.of();
            for (var currentCase : cases) {
                if (currentCase.result instanceof VariableSourceWithDependencies sourceWithDependencies) {
                    stream = Stream.concat(stream, Arrays.stream(sourceWithDependencies.getPotentialVariableNames()));
                }
                if (currentCase.condition.right().isPresent()) {
                    stream = Stream.concat(stream, Arrays.stream(currentCase.condition.right().get().potentialVariables()));
                }
            }

            return stream
                .distinct()
                .toArray(String[]::new);
        }

        private record Case(
            Either<LootCondition, WrappedExpression> condition,
            VariableSource result
        ) {
            public static final Codec<Case> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Condition.CONDITION_CODEC
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
    ) implements VariableSourceWithEquations {
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
        public double evaluate(ValueCalculatorContext context, Map<String, Double> previousVariables) {
            var value = this.value.evaluate(context, previousVariables);
            for (int i = thresholds.size() - 1; i >= 0; i--) {
                var currentThreshold = thresholds.get(i);
                if (value >= currentThreshold.threshold) {
                    return currentThreshold.result.evaluate(context, previousVariables);
                }
            }

            return fallback.evaluate(context, previousVariables);
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
            if (fallback instanceof VariableSourceWithDependencies sourceWithDependencies) {
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

    private abstract static class NbtVariableSource implements VariableSource {
        protected String nbtPath;
        protected EntitySelector entitySelector;
        protected VariableSource fallback;

        public static Codec<? extends NbtVariableSource> createCodec(
            Function3<String, EntitySelector, VariableSource, NbtVariableSource> constructor
        ) {
            return RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING
                    .fieldOf("path")
                    .forGetter(it -> it.nbtPath),
                StringIdentifiable.createCodec(EntitySelector::values)
                    .optionalFieldOf("entity", EntitySelector.THIS)
                    .forGetter(it -> it.entitySelector),
                VariableSource.CODEC
                    .optionalFieldOf("fallback", Constant.ZERO)
                    .forGetter(it -> it.fallback)
            ).apply(instance, constructor));
        }

        protected NbtVariableSource(
            String nbtPath,
            EntitySelector entitySelector,
            VariableSource fallback
        ) {
            this.nbtPath = nbtPath;
            this.entitySelector = entitySelector;
            this.fallback = fallback;
        }

        protected abstract NbtCompound getNbt(ValueCalculatorContext context);

        @Override
        public double evaluate(ValueCalculatorContext context, Map<String, Double> previousVariables) {
            var reader = new StringReader(nbtPath);
            var nbt = getNbt(context);
            if (nbt == null) {
                return fallback.evaluate(context, previousVariables);
            }

            NbtPathArgumentType.NbtPath path;
            try {
                path = NbtPathArgumentType.nbtPath().parse(reader);
            } catch (CommandSyntaxException e) {
                Gimm1q.LOGGER.warn("Failed to parse NBT path: {} in nbt {}", nbtPath, nbt);
                return fallback.evaluate(context, previousVariables);
            }

            List<NbtElement> result;
            try {
                result = path.get(nbt);
                if (result.isEmpty()) {
                    throw new Exception();
                }
            } catch (Exception e) {
                Gimm1q.LOGGER.warn("Failed to get NBT path: {} in nbt {}", nbtPath, nbt);
                return fallback.evaluate(context, previousVariables);
            }

            if (result.size() > 1) {
                Gimm1q.LOGGER.warn("Multiple values in NBT path: {} in nbt {}", nbtPath, nbt);
                return fallback.evaluate(context, previousVariables);
            }

            if (result.get(0) instanceof AbstractNbtNumber number) {
                return number.doubleValue();
            }

            Gimm1q.LOGGER.warn("Failed to get NBT path: {} in nbt {}", nbtPath, nbt);
            return fallback.evaluate(context, previousVariables);
        }
    }

    private static class EntityNbtVariableSource extends NbtVariableSource {
        public EntityNbtVariableSource(
            String nbtPath,
            EntitySelector entitySelector,
            VariableSource fallback
        ) {
            super(nbtPath, entitySelector, fallback);
        }

        @Override
        protected NbtCompound getNbt(ValueCalculatorContext context) {
            var entity = context.get(entitySelector.parameter);
            if (entity == null) {
                return new NbtCompound();
            }

            return entity.writeNbt(new NbtCompound());
        }

        @Override
        public VariableSourceType<? extends VariableSource> getType() {
            return ENTITY_NBT;
        }
    }

    private static class ItemNbtVariableSource extends NbtVariableSource {
        public ItemNbtVariableSource(
            String nbtPath,
            EntitySelector entitySelector,
            VariableSource fallback
        ) {
            super(nbtPath, entitySelector, fallback);
        }

        @Override
        protected NbtCompound getNbt(ValueCalculatorContext context) {
            var item = context.get(entitySelector.itemParameter);
            if (item == null) {
                var entity = context.get(entitySelector.parameter);
                if (entity == null) {
                    return new NbtCompound();
                }
                item = entity.getMainHandStack();
            }

            return item.getNbt();
        }

        @Override
        public List<ValueCalculatorParameter<?>> getRequiredParameters() {
            return super.getRequiredParameters();
        }

        @Override
        public VariableSourceType<? extends VariableSource> getType() {
            return ITEM_NBT;
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

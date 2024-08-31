package dev.mim1q.gimm1q;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.mim1q.gimm1q.registry.ValueCalculatorResourceReloader;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorContext;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorParameter;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.nio.file.Files;
import java.util.function.Consumer;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Gimm1qCommands {
    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(literal("gimm1q:calculate")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("type", StringArgumentType.word())
                    .suggests((context, builder) -> {
                        builder.suggest("variable");
                        builder.suggest("expression");
                        return builder.buildFuture();
                    })
                    .then(argument("id", IdentifierArgumentType.identifier())
                        .suggests((context, builder) -> {
                            ValueCalculatorResourceReloader.getAllIds().forEach(it ->
                                builder.suggest(it.toString())
                            );
                            return builder.buildFuture();
                        })
                        .then(argument("name", StringArgumentType.string())
                            .suggests((context, builder) -> {
                                var id = IdentifierArgumentType.getIdentifier(context, "id");
                                var type = StringArgumentType.getString(context, "type");
                                ValueCalculatorResourceReloader.getExpressionOrVariableNames(
                                    id,
                                    type.equals("variable")
                                ).ifPresent(set -> set.forEach(builder::suggest));
                                return builder.buildFuture();
                            })
                            .then(argument("holder", EntityArgumentType.entity())
                                .then(argument("target", EntityArgumentType.entity())
                                    .executes(context -> {
                                        var id = IdentifierArgumentType.getIdentifier(context, "id");
                                        var name = StringArgumentType.getString(context, "name");
                                        var holder = EntityArgumentType.getEntity(context, "holder");
                                        var target = EntityArgumentType.getEntity(context, "target");
                                        var type = StringArgumentType.getString(context, "type");

                                        if (!(holder instanceof LivingEntity) || !(target instanceof LivingEntity)) {
                                            context.getSource().sendError(Text.literal("Entities must be living entities"));
                                            return 1;
                                        }

                                        var value = ValueCalculatorResourceReloader.INSTANCE.calculateExpressionOrVariable(
                                            id, name, ValueCalculatorContext.create()
                                                .with(ValueCalculatorParameter.TARGET, (LivingEntity) target)
                                                .with(ValueCalculatorParameter.HOLDER, (LivingEntity) holder),
                                            type.equals("variable")
                                        );

                                        if (value.isPresent()) {
                                            context.getSource().sendFeedback(
                                                () -> Text.literal("Value calculated for " + id + "." + name + ": " + value.get()),
                                                true
                                            );
                                        } else {
                                            context.getSource().sendError(
                                                Text.literal("Value calculator not found: " + id + "." + name)
                                            );
                                            return 1;
                                        }
                                        return 0;
                                    })
                                )
                            )
                        )
                    )
                )
            )
        );


        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(literal("gimm1q:dump_value_calculators")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("holder", EntityArgumentType.entity())
                    .then(argument("target", EntityArgumentType.entity())
                        .then(argument("count", IntegerArgumentType.integer(1))
                            .executes(context -> {
                                var holder = EntityArgumentType.getEntity(context, "holder");
                                var target = EntityArgumentType.getEntity(context, "target");
                                var count = IntegerArgumentType.getInteger(context, "count");

                                return applyDumpCommand(
                                    holder,
                                    target,
                                    count,
                                    it -> context.getSource().sendFeedback(() -> it, true),
                                    it -> context.getSource().sendError(it),
                                    "value_calculators_dump"
                                );
                            })
                        )
                    )
                )
            )
        );
    }

    public static int applyDumpCommand(
        Entity holder,
        Entity target,
        int count,
        Consumer<Text> feedbackSender,
        Consumer<Text> errorSender,
        String filename
    ) {

        var ids = ValueCalculatorResourceReloader.getAllIds();
        var builder = new StringBuilder();

        for (var id : ids) {
            try {
                builder.append("## `").append(id).append("`\n");

                var variables = ValueCalculatorResourceReloader.getExpressionOrVariableNames(id, true).orElseThrow();
                builder.append("### Variables:\n");
                builder.append("| name | average time (ms) | value |").append("\n");
                builder.append("|-|-|-|").append("\n");
                for (var variable : variables) {
                    addResultsToBuilder(builder, id, count, (LivingEntity) target, (LivingEntity) holder, variable, true);
                }
                builder.append("\n");

                builder.append("### Expressions:\n");
                builder.append("| name | average time (ms) | value |").append("\n");
                builder.append("|-|-|-|").append("\n");
                var expressions = ValueCalculatorResourceReloader.getExpressionOrVariableNames(id, false).orElseThrow();
                for (var expression : expressions) {
                    addResultsToBuilder(builder, id, count, (LivingEntity) target, (LivingEntity) holder, expression, false);
                }
                builder.append("\n---\n");
            } catch (Exception e) {
                Gimm1q.LOGGER.error("Failed to dump value calculator: {}", id, e);
            }
        }

        var relativePath = "logs/gimm1q/" + filename + ".md";

        feedbackSender.accept(Text.literal("Result dumped to file " + relativePath));

        try {
            var path = FabricLoader.getInstance().getGameDir();
            var file = path.resolve(relativePath);

            Files.createDirectories(file.getParent());
            if (!Files.exists(file)) {
                Files.createFile(file);
            }
            Files.writeString(file, builder.toString());

        } catch (Exception e) {
            errorSender.accept(Text.literal("Failed to save file: " + e.getMessage()));
            return 1;
        }

        return 0;
    }

    private static void addResultsToBuilder(StringBuilder builder, Identifier id, int count, LivingEntity target, LivingEntity holder, String name, boolean variable) {
        Double firstResult = null;
        float totalTime = 0f;

        for (int i = 0; i < count; i++) {
            var startTime = System.nanoTime();
            var value = ValueCalculatorResourceReloader.INSTANCE.calculateExpressionOrVariable(
                id, name, ValueCalculatorContext.create()
                    .with(ValueCalculatorParameter.TARGET, target)
                    .with(ValueCalculatorParameter.HOLDER, holder),
                variable
            );
            var time = (System.nanoTime() - startTime) / 1_000_000f;
            if (value.isPresent() && firstResult == null) {
                firstResult = value.get();
            }
            totalTime += time;
        }

        var averageTime = totalTime / count;
        builder
            .append("|").append(name)
            .append("|").append(String.format("%.8f", averageTime))
            .append("|").append(firstResult == null ? "-" : firstResult);
        builder.append("|\n");
    }
}

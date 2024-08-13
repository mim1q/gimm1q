package dev.mim1q.gimm1q;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.mim1q.gimm1q.registry.ValueCalculatorResourceReloader;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorContext;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorParameter;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;

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
    }
}

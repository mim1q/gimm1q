package dev.mim1q.gimm1q;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.mim1q.gimm1q.registry.ValueCalculatorResourceReloader;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorContext;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorParameter;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Gimm1qCommands {

    void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(literal("gimm1q:calculate")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("type", null)
                    .then(argument("id", IdentifierArgumentType.identifier())
                        .then(argument("name", StringArgumentType.string())
                            .then(argument("holder", EntityArgumentType.entity())
                                .then(argument("target", EntityArgumentType.entity())
                                    .executes(context -> {
                                        var id = IdentifierArgumentType.getIdentifier(context, "id");
                                        var name = StringArgumentType.getString(context, "name");
                                        var holder = EntityArgumentType.getEntity(context, "holder");
                                        var target = EntityArgumentType.getEntity(context, "target");
                                        var type = context.getArgument("type", EnumArgumentType.class);

                                        if (!(holder instanceof LivingEntity) || !(target instanceof LivingEntity)) {
                                            context.getSource().sendError(Text.literal("Entities must be living entities"));
                                            return 1;
                                        }

                                        var value = ValueCalculatorResourceReloader.INSTANCE.calculate(
                                            id, name, ValueCalculatorContext.create()
                                                .with(ValueCalculatorParameter.TARGET, (LivingEntity) target)
                                                .with(ValueCalculatorParameter.HOLDER, (LivingEntity) holder)
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

    private enum ValueCalculatorCommandType implements StringIdentifiable {
        EXPRESSION("expression"),
        VARIABLE("variable");

        private final String name;

        ValueCalculatorCommandType(String name) {
            this.name = name;
        }


        @Override
        public String asString() {
            return name;
        }
    }
}

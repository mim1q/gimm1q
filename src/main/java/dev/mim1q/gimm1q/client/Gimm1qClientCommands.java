package dev.mim1q.gimm1q.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;

import static dev.mim1q.gimm1q.Gimm1qCommands.applyDumpCommand;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;


public class Gimm1qClientCommands {
    public static void init() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("gimm1q:dump_value_calculators_client")
                .then(argument("count", IntegerArgumentType.integer())
                    .executes(context -> {
                        int count = IntegerArgumentType.getInteger(context, "count");
                        var holder = context.getSource().getPlayer();
                        var target = context.getSource().getWorld().getClosestEntity(
                            LivingEntity.class,
                            TargetPredicate.DEFAULT,
                            holder,
                            holder.getX(),
                            holder.getY(),
                            holder.getZ(),
                            holder.getBoundingBox().expand(1024.0)
                        );

                        if (target == null) {
                            return 1;
                        }

                        applyDumpCommand(
                            holder,
                            target,
                            count,
                            it -> context.getSource().sendFeedback(it),
                            it -> context.getSource().sendError(it),
                            "value_calculators_dump_client"
                        );

                        return 0;
                    })
                )
            );
        });
    }
}

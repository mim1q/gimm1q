package dev.mim1q.gimm1q;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.mim1q.gimm1q.registry.ValueCalculatorResourceReloader;
import dev.mim1q.gimm1q.valuecalculators.ValueCalculator;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorContext;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorParameter;
import dev.mim1q.gimm1q.valuecalculators.variables.VariableSourceTypes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@ApiStatus.Internal
public class Gimm1q implements ModInitializer {
    public static final String ID = "gimm1q";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Gimm1q - Mim1q's Fabric modding library!");

        // Registering custom resources
        VariableSourceTypes.init();
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(ValueCalculatorResourceReloader.INSTANCE);

        // Utility command for testing Value Calculators
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("gimm1q:calculate_value")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("id", IdentifierArgumentType.identifier())
                    .then(argument("name", StringArgumentType.string())
                        .then(argument("holder", EntityArgumentType.entity())
                            .then(argument("target", EntityArgumentType.entity())
                                .executes(context -> {
                                    var id = IdentifierArgumentType.getIdentifier(context, "id");
                                    var name = StringArgumentType.getString(context, "name");
                                    var holder = EntityArgumentType.getEntity(context, "holder");
                                    var target = EntityArgumentType.getEntity(context, "target");

                                    if (!(holder instanceof LivingEntity) || !(target instanceof LivingEntity)) {
                                        context.getSource().sendError(Text.literal("Entities must be living entities"));
                                        return 1;
                                    }

                                    var value = ValueCalculatorResourceReloader.INSTANCE.calculate(
                                        id, name, ValueCalculatorContext.create()
                                            .with(ValueCalculatorParameter.TARGET, (LivingEntity) target)
                                            .with(ValueCalculatorParameter.HOLDER, (LivingEntity) holder)
                                    );

                                    context.getSource().sendFeedback(
                                        () -> Text.literal("Value calculated for " + id + "." + name + ": " + value),
                                        true
                                    );

                                    return 0;
                                })
                            )
                        )
                    )
                )
            );
        });
    }

    public static Identifier id(String path) {
        return new Identifier(ID, path);
    }
}

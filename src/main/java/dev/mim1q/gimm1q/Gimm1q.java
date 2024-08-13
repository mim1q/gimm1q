package dev.mim1q.gimm1q;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.mim1q.gimm1q.registry.ValueCalculatorResourceReloader;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorContext;
import dev.mim1q.gimm1q.valuecalculators.parameters.ValueCalculatorParameter;
import dev.mim1q.gimm1q.valuecalculators.variables.VariableSourceTypes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
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
    }

    public static Identifier id(String path) {
        return new Identifier(ID, path);
    }
}

package dev.mim1q.gimm1q;

import dev.mim1q.gimm1q.network.Gimm1qPacketIds;
import dev.mim1q.gimm1q.registry.ValueCalculatorResourceReloader;
import dev.mim1q.gimm1q.valuecalculators.variables.VariableSourceTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class Gimm1q implements ModInitializer {
    public static final String ID = "gimm1q";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    /**
     * Only use for the testmod. Don't enable in production because there's lots of debug messages
     */
    @ApiStatus.Internal
    public static boolean debugMessages = false;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Gimm1q - Mim1q's Fabric modding library!");

        // Registering custom resources
        VariableSourceTypes.init();
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(ValueCalculatorResourceReloader.INSTANCE);

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
                sender.sendPacket(
                    Gimm1qPacketIds.SYNC_VALUE_CALCULATORS_S2C,
                    ValueCalculatorResourceReloader.INSTANCE.createPacket()
                );
            });
            ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
                ServerPlayNetworking.send(
                    player,
                    Gimm1qPacketIds.SYNC_VALUE_CALCULATORS_S2C,
                    ValueCalculatorResourceReloader.INSTANCE.createPacket()
                );
            });
        }

        // Custom commands
        Gimm1qCommands.init();
    }

    public static Identifier id(String path) {
        return new Identifier(ID, path);
    }
}

package dev.mim1q.gimm1q.network;

import dev.mim1q.gimm1q.network.s2c.CameraShakeS2CPacket;
import dev.mim1q.gimm1q.network.s2c.ValueCalculatorSyncS2CPacket;
import dev.mim1q.gimm1q.registry.ValueCalculatorResourceReloader;
import dev.mim1q.gimm1q.screenshake.ScreenShakeAccessor;
import dev.mim1q.gimm1q.screenshake.ScreenShakeModifiers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class Gimm1qClientNetworkHandler {
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(CameraShakeS2CPacket.ID, (msg, ctx) -> {
            ClientPlayerEntity player = ctx.player();
            if (player == null) return;
            var intensity = msg.intensity();
            if (!msg.modifierName().isBlank()) {
                intensity *= ScreenShakeModifiers.getModifier(msg.modifierName());
            }
            ((ScreenShakeAccessor) player).shakeCamera(intensity, msg.duration());
        });

        ClientPlayNetworking.registerGlobalReceiver(ValueCalculatorSyncS2CPacket.ID, (msg, ctx) ->
            ctx.client().executeTask(() -> ValueCalculatorResourceReloader.INSTANCE.replaceWith(msg.map()))
        );
    }
}

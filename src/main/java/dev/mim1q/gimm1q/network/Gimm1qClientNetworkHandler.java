package dev.mim1q.gimm1q.network;

import dev.mim1q.gimm1q.interfaces.ShakeableCameraAccessor;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;

public class Gimm1qClientNetworkHandler {
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(Gimm1qPacketIds.CAMERA_SHAKE_S2C, (client, handler, buf, responseSender) -> {
            float shakeIntensity = buf.readFloat();
            int shakeDuration = buf.readInt();
            ClientPlayerEntity player = client.player;
            if (player == null) return;
            ((ShakeableCameraAccessor) player).shakeCamera(shakeIntensity, shakeDuration);
        });
    }
}

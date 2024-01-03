package dev.mim1q.gimm1q.util;

import dev.mim1q.gimm1q.network.s2c.CameraShakeS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerCameraShake {
    public static void applyShake(ServerPlayerEntity player, float intensity, int duration) {
        new CameraShakeS2CPacket(intensity, duration).send(player);
    }
}

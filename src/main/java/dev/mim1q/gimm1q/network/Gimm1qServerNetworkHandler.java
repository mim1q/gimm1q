package dev.mim1q.gimm1q.network;

import dev.mim1q.gimm1q.network.s2c.CameraShakeS2CPacket;
import dev.mim1q.gimm1q.network.s2c.ValueCalculatorSyncS2CPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class Gimm1qServerNetworkHandler {
    public static void init() {
        PayloadTypeRegistry.playS2C().register(CameraShakeS2CPacket.ID, CameraShakeS2CPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(ValueCalculatorSyncS2CPacket.ID, ValueCalculatorSyncS2CPacket.CODEC);
    }
}

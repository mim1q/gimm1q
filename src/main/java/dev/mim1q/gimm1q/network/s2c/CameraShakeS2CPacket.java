package dev.mim1q.gimm1q.network.s2c;

import dev.mim1q.gimm1q.network.Gimm1qPacketIds;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class CameraShakeS2CPacket extends PacketByteBuf {
    public CameraShakeS2CPacket(float intensity, int duration) {
        super(Unpooled.buffer());
        writeFloat(intensity);
        writeInt(duration);
    }

    public void send(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, Gimm1qPacketIds.CAMERA_SHAKE_S2C, this);
    }
}

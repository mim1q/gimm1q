package dev.mim1q.gimm1q.network.s2c;

import dev.mim1q.gimm1q.network.Gimm1qPacketIds;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class CameraShakeS2CPacket extends PacketByteBuf {
    public CameraShakeS2CPacket(float intensity, int duration, String modifierName) {
        super(Unpooled.buffer());
        writeFloat(intensity);
        writeInt(duration);
        writeString(modifierName);
    }

    public void send(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, Gimm1qPacketIds.CAMERA_SHAKE_S2C, this);
    }
}

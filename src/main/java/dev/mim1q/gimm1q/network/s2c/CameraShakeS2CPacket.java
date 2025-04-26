package dev.mim1q.gimm1q.network.s2c;

import dev.mim1q.gimm1q.Gimm1q;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record CameraShakeS2CPacket(
    float intensity,
    int duration,
    String modifierName
) implements CustomPayload {
    public static final Id<CameraShakeS2CPacket> ID = new Id<>(Gimm1q.id("camera_shake"));
    public static final PacketCodec<RegistryByteBuf, CameraShakeS2CPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.FLOAT, CameraShakeS2CPacket::intensity,
        PacketCodecs.INTEGER, CameraShakeS2CPacket::duration,
        PacketCodecs.STRING, CameraShakeS2CPacket::modifierName,
        CameraShakeS2CPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
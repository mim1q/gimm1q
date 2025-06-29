package dev.mim1q.gimm1q.particle.electric;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.Vec3d;

public class ElectricityParticleType extends ParticleType<ElectricityParticleEffect> {
    protected ElectricityParticleType(boolean alwaysShow) {
        super(alwaysShow);
    }

    public ElectricityParticleEffect get(Vec3d direction, int length, int color, float size) {
        return new ElectricityParticleEffect(direction, length, color, size, true);
    }

    public static ElectricityParticleType create() {
        return new ElectricityParticleType(true);
    }

    @Override
    public MapCodec<ElectricityParticleEffect> getCodec() {
        return ElectricityParticleEffect.CODEC;
    }

    @Override
    public PacketCodec<? super RegistryByteBuf, ElectricityParticleEffect> getPacketCodec() {
        return ElectricityParticleEffect.PACKET_CODEC;
    }
}

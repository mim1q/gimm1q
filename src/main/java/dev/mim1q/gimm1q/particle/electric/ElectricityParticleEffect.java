package dev.mim1q.gimm1q.particle.electric;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mim1q.gimm1q.registry.Gimm1qParticles;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.Vec3d;

public record ElectricityParticleEffect(
    Vec3d direction,
    int length,
    int color,
    float size,
    boolean isMainBranch
) implements ParticleEffect {
    public static final MapCodec<ElectricityParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Vec3d.CODEC.fieldOf("direction").forGetter(ElectricityParticleEffect::direction),
        Codec.INT.fieldOf("length").forGetter(ElectricityParticleEffect::length),
        Codec.INT.fieldOf("color").forGetter(ElectricityParticleEffect::color),
        Codec.FLOAT.fieldOf("size").forGetter(ElectricityParticleEffect::size),
        Codec.BOOL.fieldOf("isMainBranch").forGetter(ElectricityParticleEffect::isMainBranch)
    ).apply(instance, ElectricityParticleEffect::new));

    public static final PacketCodec<RegistryByteBuf, ElectricityParticleEffect> PACKET_CODEC = PacketCodecs.registryCodec(CODEC.codec());

    @Override
    public ParticleType<?> getType() {
        return Gimm1qParticles.ELECTRICITY;
    }
}

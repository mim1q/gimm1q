package dev.mim1q.gimm1q.particle.electric;

import com.mojang.serialization.Codec;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.Vec3d;

public class ElectricityParticleType extends ParticleType<ElectricityParticleEffect> {
    @SuppressWarnings("deprecation")
    protected ElectricityParticleType(boolean alwaysShow, ParticleEffect.Factory<ElectricityParticleEffect> parametersFactory) {
        super(alwaysShow, parametersFactory);
    }

    public ElectricityParticleEffect get(Vec3d direction, int length, int color, float size) {
        return new ElectricityParticleEffect(direction, length, color, size, true);
    }

    public static ElectricityParticleType create() {
        return new ElectricityParticleType(true, ElectricityParticleEffect.PARAMETERS_FACTORY);
    }

    @Override
    public Codec<ElectricityParticleEffect> getCodec() {
        return ElectricityParticleEffect.CODEC;
    }
}

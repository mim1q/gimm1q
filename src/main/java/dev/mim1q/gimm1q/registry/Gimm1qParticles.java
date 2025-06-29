package dev.mim1q.gimm1q.registry;

import dev.mim1q.gimm1q.Gimm1q;
import dev.mim1q.gimm1q.particle.electric.ElectricityParticle;
import dev.mim1q.gimm1q.particle.electric.ElectricityParticleType;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class Gimm1qParticles {
    public static final ElectricityParticleType ELECTRICITY = ElectricityParticleType.create();

    public static void init() {
        Registry.register(Registries.PARTICLE_TYPE, Gimm1q.id("electricity"), ELECTRICITY);
    }

    public static void initClient() {
        ParticleFactoryRegistry.getInstance().register(ELECTRICITY, ElectricityParticle.Factory::new);
    }
}

package dev.mim1q.testmod.item;

import dev.mim1q.gimm1q.particle.electric.ElectricityParticleEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ParticleTesterItem extends Item {
    public ParticleTesterItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient) {
            var particle = new ElectricityParticleEffect(
                user.getRotationVector().multiply(1.0, 0.0 ,1.0),
                6,
                0xC0EEFF,
                1.0f,
                true
            );

            world.addParticle(
                particle,
                user.getX(),
                user.getY() + 10.0,
                user.getZ(),
                0.0,
                0.0,
                0.0
            );
        }

        return super.use(world, user, hand);
    }
}

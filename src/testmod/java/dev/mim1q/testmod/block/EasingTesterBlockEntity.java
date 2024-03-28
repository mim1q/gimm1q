package dev.mim1q.testmod.block;

import dev.mim1q.gimm1q.interpolation.AnimatedProperty;
import dev.mim1q.gimm1q.interpolation.AnimatedProperty.EasingFunction;
import dev.mim1q.testmod.TestMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

public class EasingTesterBlockEntity extends BlockEntity {
    public final AnimatedProperty animatedProperty = new AnimatedProperty(0.0F);

    public EasingTesterBlockEntity(BlockPos pos, BlockState state) {
        super(TestMod.EASING_TESTER_BE, pos, state);
    }

    public EasingFunction getEasingFunction() {
        return getCachedState().get(EasingTesterBlock.EASING_TYPE).easingFunction;
    }

    public static void tick(EasingTesterBlockEntity blockEntity) {
        if (!(blockEntity.getWorld() instanceof ClientWorld)) return;
        var time = blockEntity.getWorld().getTime();

        if (time % 60 == 20) {
            blockEntity.animatedProperty.transitionTo(1.0F, 20.0F, blockEntity.getEasingFunction());
        } else if (time % 60 == 50) {
            blockEntity.animatedProperty.transitionTo(0.0F, 20.0F, blockEntity.getEasingFunction());
        }
    }
}

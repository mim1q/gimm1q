package dev.mim1q.testmod.block;

import dev.mim1q.gimm1q.screenshake.ScreenShakeUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ThumperBlock extends Block {
    public ThumperBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        world.scheduleBlockTick(pos, this, 1);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        ScreenShakeUtils.shakeAround(
            world,
            Vec3d.ofBottomCenter(pos),
            2f,
            20,
            2,
            5,
            "testmod"
        );
        world.scheduleBlockTick(pos, this, 20);
    }
}

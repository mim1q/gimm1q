package dev.mim1q.testmod.block;

import dev.mim1q.gimm1q.interpolation.AnimatedProperty.EasingFunction;
import dev.mim1q.gimm1q.interpolation.Easing;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EasingTesterBlock extends BlockWithEntity {
    public static EnumProperty<EasingType> EASING_TYPE = EnumProperty.of("easing_type", EasingType.class);

    public EasingTesterBlock(Settings settings) {
        super(settings.nonOpaque());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(EASING_TYPE);
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            var easingOffset = player.isSneaking() ? -1 : 1;
            var nextEasingInt = state.get(EASING_TYPE).ordinal() + easingOffset;
            var nextEasing = EasingType.values()[Math.floorMod(nextEasingInt, EasingType.values().length)];
            world.setBlockState(pos, state.with(EASING_TYPE, nextEasing));
            player.sendMessage(Text.literal("Easing type: " + nextEasing.name), true);
        }
        return ActionResult.success(true);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EasingTesterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return (world1, pos, state1, blockEntity) -> EasingTesterBlockEntity.tick((EasingTesterBlockEntity) blockEntity);
    }

    @SuppressWarnings("unused")
    public enum EasingType implements StringIdentifiable {
        LINEAR("linear", Easing::clampedLerp),
        EASE_IN_QUAD("ease_in_quad", Easing::easeInQuad),
        EASE_OUT_QUAD("ease_out_quad", Easing::easeOutQuad),
        EASE_IN_OUT_QUAD("ease_in_out_quad", Easing::easeInOutQuad),
        EASE_IN_CUBIC("ease_in_cubic", Easing::easeInCubic),
        EASE_OUT_CUBIC("ease_out_cubic", Easing::easeOutCubic),
        EASE_IN_OUT_CUBIC("ease_in_out_cubic", Easing::easeInOutCubic),
        EASE_IN_BACK("ease_in_back", Easing::easeInBack),
        EASE_OUT_BACK("ease_out_back", Easing::easeOutBack),
        EASE_IN_OUT_BACK("ease_in_out_back", Easing::easeInOutBack),
        EASE_IN_ELASTIC("ease_in_elastic", Easing::easeInElastic),
        EASE_OUT_ELASTIC("ease_out_elastic", Easing::easeOutElastic),
        EASE_IN_OUT_ELASTIC("ease_in_out_elastic", Easing::easeInOutElastic),
        EASE_OUT_BOUNCE("ease_out_bounce", Easing::easeOutBounce);


        public final String name;
        public final EasingFunction easingFunction;

        EasingType(String name, EasingFunction easingFunction) {
            this.name = name;
            this.easingFunction = easingFunction;
        }

        @Override
        public String asString() {
            return name;
        }
        }
}

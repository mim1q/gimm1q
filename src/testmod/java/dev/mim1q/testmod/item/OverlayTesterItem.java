package dev.mim1q.testmod.item;

import dev.mim1q.gimm1q.Gimm1q;
import dev.mim1q.gimm1q.client.render.overlay.ModelOverlayVertexConsumer;
import dev.mim1q.gimm1q.client.render.overlay.OverlayUvMapper;
import dev.mim1q.gimm1q.client.render.WrapperVertexConsumer;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static dev.mim1q.gimm1q.client.render.overlay.OverlayUvMapper.*;

public class OverlayTesterItem extends Item {
    // Important note:
    // OverlayUvMappers should be created like this, and not in the VertexConsumer builder, to prevent instantiating
    // them every frame.
    private static final OverlayUvMapper FRAME_ANIMATION_UV_MAPPER = frameAnimation(4, 10f);
    private static final OverlayUvMapper VERTICALLY_SCROLLING_UV_MAPPER = verticalScrollAnimation(0.05f);
    private static final OverlayUvMapper HORIZONTALLY_SCROLLING_UV_MAPPER = horizontalScrollAnimation(0.05f);
    private static final OverlayUvMapper DIAGONALLY_SCROLLING_UV_MAPPER = diagonalScrollAnimation(0.05f, -0.02f);

    public OverlayTesterItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        var stack = user.getStackInHand(hand);
        var nbt = stack.getOrCreateNbt();
        if (!nbt.contains("overlay")) {
            nbt.putInt("overlay", 0);
        } else {
            var newI = (nbt.getInt("overlay") + 1) % 8;
            nbt.putInt("overlay", newI);
            user.sendMessage(Text.literal(getOverlayName(newI)), true);
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        int i = stack.getOrCreateNbt().getInt("overlay");
        tooltip.add(Text.literal(getOverlayName(i)));
    }

    private static String getOverlayName(int i) {
        return switch (i) {
            case 1 -> "Static 16x16 Overlay";
            case 2 -> "Static 64x64 Overlay, nonstandard offset";
            case 3 -> "Outline Overlay";
            case 4 -> "Frame Animation Overlay";
            case 5 -> "Vertically Scrolling Overlay";
            case 6 -> "Horizontally Scrolling Overlay";
            case 7 -> "Diagonally Scrolling Overlay";
            default -> "No Overlay";
        };
    }

    public static WrapperVertexConsumer getVertexConsumer(VertexConsumerProvider vertexConsumers, ItemStack stack) {
        int i = stack.getOrCreateNbt().getInt("overlay");

        var consumer = switch (i) {
            case 1, 5, 6, 7 ->
                vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(new Identifier("textures/block/stone.png")));
            case 2 ->
                vertexConsumers.getBuffer(RenderLayer.getEntityCutout(new Identifier("textures/entity/zombie/zombie.png")));
            case 3 -> vertexConsumers.getBuffer(RenderLayer.getEyes(Gimm1q.id("textures/block/white.png")));
            case 4 ->
                vertexConsumers.getBuffer(RenderLayer.getEntityTranslucentEmissive(new Identifier("textures/block/prismarine.png")));
            default -> null;
        };

        if (consumer == null) return null;

        return switch (i) {
            case 1 -> ModelOverlayVertexConsumer.of(consumer);
            case 2 -> ModelOverlayVertexConsumer.of(consumer).textureSize(64).offset(0.2f);
            case 3 -> ModelOverlayVertexConsumer.of(consumer).inverted().skipPlanes();
            case 4 -> ModelOverlayVertexConsumer.of(consumer).mapUv(FRAME_ANIMATION_UV_MAPPER);
            case 5 -> ModelOverlayVertexConsumer.of(consumer).mapUv(VERTICALLY_SCROLLING_UV_MAPPER);
            case 6 -> ModelOverlayVertexConsumer.of(consumer).mapUv(HORIZONTALLY_SCROLLING_UV_MAPPER);
            case 7 -> ModelOverlayVertexConsumer.of(consumer).mapUv(DIAGONALLY_SCROLLING_UV_MAPPER);
            default -> null;
        };
    }
}

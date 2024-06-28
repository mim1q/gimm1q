package dev.mim1q.testmod.item;

import dev.mim1q.gimm1q.Gimm1q;
import dev.mim1q.gimm1q.client.render.ModelOverlayVertexConsumer;
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

import static dev.mim1q.gimm1q.client.render.ModelOverlayVertexConsumer.OverlayUvMapper.*;

public class OverlayTesterItem extends Item {
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
            case 3 ->
                vertexConsumers.getBuffer(RenderLayer.getEyes(Gimm1q.id("textures/block/white.png")));
            case 4 ->
                vertexConsumers.getBuffer(RenderLayer.getEntityTranslucentEmissive(new Identifier("textures/block/fire_0.png")));
            default -> null;
        };

        if (consumer == null) return null;

        return switch (i) {
            case 1 -> ModelOverlayVertexConsumer.of(consumer).skipPlanes();
            case 2 -> ModelOverlayVertexConsumer.of(consumer).textureSize(64).offset(0.2f);
            case 3 -> ModelOverlayVertexConsumer.of(consumer).inverted().offset(0.5f);
            case 4 -> ModelOverlayVertexConsumer.of(consumer).mapUv(frameAnimation(32, 1f));
            case 5 -> ModelOverlayVertexConsumer.of(consumer).mapUv(verticalScrollAnimation(0.02f));
            case 6 -> ModelOverlayVertexConsumer.of(consumer).mapUv(horizontalScrollAnimation(0.04f));
            case 7 -> ModelOverlayVertexConsumer.of(consumer).mapUv(diagonalScrollAnimation(0.02f, -0.02f));
            default -> null;
        };
    }
}

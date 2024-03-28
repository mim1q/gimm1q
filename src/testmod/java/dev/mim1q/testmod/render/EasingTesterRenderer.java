package dev.mim1q.testmod.render;

import dev.mim1q.testmod.block.EasingTesterBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

public class EasingTesterRenderer implements BlockEntityRenderer<EasingTesterBlockEntity> {
    private static final BlockState RENDERED_BLOCK = Blocks.DIAMOND_BLOCK.getDefaultState();

    private final BlockRenderManager blockRenderer;

    public EasingTesterRenderer(BlockEntityRendererFactory.Context context) {
        this.blockRenderer = context.getRenderManager();
    }


    @Override
    public void render(EasingTesterBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.getWorld() == null) return;

        entity.animatedProperty.update(entity.getWorld().getTime() + tickDelta);
        matrices.push();
        {
            matrices.translate(0.0, entity.animatedProperty.getValue() * 2, 0.0);
            blockRenderer.renderBlock(
                RENDERED_BLOCK,
                entity.getPos(),
                entity.getWorld(),
                matrices,
                vertexConsumers.getBuffer(RenderLayer.getSolid()),
                false,
                entity.getWorld().random
            );

        }
        matrices.pop();
    }
}

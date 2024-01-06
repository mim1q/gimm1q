package dev.mim1q.gimm1q.client.highlight;

import dev.mim1q.gimm1q.Gimm1q;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class HighlightDrawerImpl implements HighlightDrawer {
    public static final HighlightDrawerImpl INSTANCE = new HighlightDrawerImpl();
    private static final Identifier TEXTURE = Gimm1q.id("textures/block/white.png");

    private WorldRenderContext renderContext = null;

    private HighlightDrawerImpl() {}

    public static void setRenderContext(WorldRenderContext renderContext) {
        INSTANCE.renderContext = renderContext;
    }

    @Override
    public void drawHighlight(Vec3d from, Vec3d to, int color) {
        if (renderContext == null) return;

        var matrices = renderContext.matrixStack();
        var vertexConsumers = renderContext.consumers();
        if (matrices == null || vertexConsumers == null) return;

        var buffer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(TEXTURE));
        var vertices = new float[][] {
            new float[] { (float) from.x, (float) from.y, (float) from.z }, // 000
            new float[] { (float) from.x, (float) from.y, (float) to.z }, // 001
            new float[] { (float) from.x, (float) to.y, (float) from.z }, // 010
            new float[] { (float) from.x, (float) to.y, (float) to.z },   // 011
            new float[] { (float) to.x, (float) from.y, (float) from.z }, // 100
            new float[] { (float) to.x, (float) from.y, (float) to.z },   // 101
            new float[] { (float) to.x, (float) to.y, (float) from.z },   // 110
            new float[] { (float) to.x, (float) to.y, (float) to.z },     // 111
        };

        matrices.push();
        {
            matrices.translate(-renderContext.camera().getPos().x, -renderContext.camera().getPos().y, -renderContext.camera().getPos().z);
            drawFace(matrices, buffer, new float[][] { vertices[3], vertices[2], vertices[0], vertices[1] }, color);
            drawFace(matrices, buffer, new float[][] { vertices[6], vertices[7], vertices[5], vertices[4] }, color);
            drawFace(matrices, buffer, new float[][] { vertices[2], vertices[6], vertices[4], vertices[0] }, color);
            drawFace(matrices, buffer, new float[][] { vertices[7], vertices[3], vertices[1], vertices[5] }, color);
            drawFace(matrices, buffer, new float[][] { vertices[4], vertices[5], vertices[1], vertices[0] }, color);
            drawFace(matrices, buffer, new float[][] { vertices[3], vertices[7], vertices[6], vertices[2] }, color);
        }
        matrices.pop();
    }

    private void drawVertex(Matrix4f posMatrix, Matrix3f normalMatrix, VertexConsumer buffer, float[][] vertices, int argb, int index) {
        var u = index & 1;
        var v = (index >> 1) & 1;
        buffer
            .vertex(posMatrix, vertices[index][0], vertices[index][1], vertices[index][2])
            .color(argb)
            .texture(u, v)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(0x0000F0)
            .normal(normalMatrix, 0.0F, 1.0F, 0.0F)
            .next();
    }

    private void drawFace(MatrixStack matrices, VertexConsumer buffer, float[][] vertices, int argb) {
        var posMatrix = matrices.peek().getPositionMatrix();
        var normalMatrix = matrices.peek().getNormalMatrix();

        for (int i = 0 ; i <= 3; ++i) {
            drawVertex(posMatrix, normalMatrix, buffer, vertices, argb, i);
        }
    }
}

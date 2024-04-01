package dev.mim1q.gimm1q.client.highlight;

import dev.mim1q.gimm1q.Gimm1q;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import static net.minecraft.util.math.MathHelper.sign;

@ApiStatus.Internal
public class HighlightDrawerImpl implements HighlightDrawer {
    public static final HighlightDrawerImpl INSTANCE = new HighlightDrawerImpl();
    private static final Identifier TEXTURE = Gimm1q.id("textures/block/white.png");

    private WorldRenderContext renderContext = null;

    private HighlightDrawerImpl() {}

    public static void setRenderContext(WorldRenderContext renderContext) {
        INSTANCE.renderContext = renderContext;
    }

    @Override
    public void drawHighlight(Box box, int colorArgb, int outlineArgb) {
        if (renderContext == null) return;

        var matrices = renderContext.matrixStack();
        var vertexConsumers = renderContext.consumers();
        if (matrices == null || vertexConsumers == null) return;

        var vertices = new float[][] {
            new float[] { (float) box.minX, (float) box.minY, (float) box.minZ }, // 000 [0]
            new float[] { (float) box.minX, (float) box.minY, (float) box.maxZ }, // 001 [1]
            new float[] { (float) box.minX, (float) box.maxY, (float) box.minZ }, // 010 [2]
            new float[] { (float) box.minX, (float) box.maxY, (float) box.maxZ }, // 011 [3]
            new float[] { (float) box.maxX, (float) box.minY, (float) box.minZ }, // 100 [4]
            new float[] { (float) box.maxX, (float) box.minY, (float) box.maxZ }, // 101 [5]
            new float[] { (float) box.maxX, (float) box.maxY, (float) box.minZ }, // 110 [6]
            new float[] { (float) box.maxX, (float) box.maxY, (float) box.maxZ }, // 111 [7]
        };

        matrices.push();
        {
            matrices.translate(-renderContext.camera().getPos().x, -renderContext.camera().getPos().y, -renderContext.camera().getPos().z);
            drawFace(matrices, vertexConsumers, new float[][] { vertices[3], vertices[2], vertices[0], vertices[1] }, colorArgb, outlineArgb);
            drawFace(matrices, vertexConsumers, new float[][] { vertices[6], vertices[7], vertices[5], vertices[4] }, colorArgb, outlineArgb);
            drawFace(matrices, vertexConsumers, new float[][] { vertices[2], vertices[6], vertices[4], vertices[0] }, colorArgb, outlineArgb);
            drawFace(matrices, vertexConsumers, new float[][] { vertices[7], vertices[3], vertices[1], vertices[5] }, colorArgb, outlineArgb);
            drawFace(matrices, vertexConsumers, new float[][] { vertices[4], vertices[5], vertices[1], vertices[0] }, colorArgb, outlineArgb);
            drawFace(matrices, vertexConsumers, new float[][] { vertices[3], vertices[7], vertices[6], vertices[2] }, colorArgb, outlineArgb);
        }
        matrices.pop();
    }

    private void drawVertex(Matrix4f posMatrix, Matrix3f normalMatrix, VertexConsumer buffer, float[] vertex, int argb, float u, float v) {
        buffer
            .vertex(posMatrix, vertex[0], vertex[1], vertex[2])
            .color(argb)
            .texture(u, v)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(0x0000F0)
            .normal(normalMatrix, 0.0F, 1.0F, 0.0F)
            .next();
    }

    private void drawFace(MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, float[][] vertices, int argb, int outlineArgb) {
        var posMatrix = matrices.peek().getPositionMatrix();
        var normalMatrix = matrices.peek().getNormalMatrix();
        var buffer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucent(TEXTURE));

        var drawOutline = (outlineArgb & 0xFF000000) != 0;
        var innerVertices = vertices;

        if (drawOutline) {
            innerVertices = getInsetFace(vertices, 1/16f);
            var outlineBuffer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucent(TEXTURE));
            drawInsetFace(posMatrix, normalMatrix, outlineBuffer, vertices, innerVertices, outlineArgb);
        }

        var drawInner = (argb & 0xFF000000) != 0;
        if (!drawInner) return;

        for (var i = 0 ; i <= 3; ++i) {
            var u = i & 1;
            var v = (i >> 1) & 1;
            drawVertex(posMatrix, normalMatrix, buffer, innerVertices[i], argb, u, v);
        }
    }

    private void drawInsetFace(Matrix4f posMatrix, Matrix3f normalMatrix, VertexConsumer buffer, float[][] vertices, float[][] insetVertices, int argb) {
        for (var i = 0 ; i <= 3; ++i) {
            drawVertex(posMatrix, normalMatrix, buffer, vertices[i], argb, 0, 0);
            drawVertex(posMatrix, normalMatrix, buffer, insetVertices[i], argb, 1, 0);
            drawVertex(posMatrix, normalMatrix, buffer, insetVertices[(i + 1) % 4], argb, 1, 1);
            drawVertex(posMatrix, normalMatrix, buffer, vertices[(i + 1) % 4], argb, 0, 1);
        }
    }

    private float[][] getInsetFace(float[][] vertices, float inset) {
        float dx = sign(vertices[2][0] - vertices[0][0]);
        float dy = sign(vertices[2][1] - vertices[0][1]);
        float dz = sign(vertices[2][2] - vertices[0][2]);

        if (dy == 0) {
            if (dz > 0) {
                return new float[][] {
                    new float[] { vertices[0][0] + dx * inset, vertices[0][1], vertices[0][2] + dz * inset },
                    new float[] { vertices[1][0] + dx * inset, vertices[1][1], vertices[1][2] - dz * inset },
                    new float[] { vertices[2][0] - dx * inset, vertices[2][1], vertices[2][2] - dz * inset },
                    new float[] { vertices[3][0] - dx * inset, vertices[3][1], vertices[3][2] + dz * inset },
                };
            }
            return new float[][] {
                new float[] { vertices[0][0] + dx * inset, vertices[0][1], vertices[0][2] + dz * inset },
                new float[] { vertices[1][0] - dx * inset, vertices[1][1], vertices[1][2] + dz * inset },
                new float[] { vertices[2][0] - dx * inset, vertices[2][1], vertices[2][2] - dz * inset },
                new float[] { vertices[3][0] + dx * inset, vertices[3][1], vertices[3][2] - dz * inset },
            };
        }

        return new float[][] {
          new float[] { vertices[0][0] + dx * inset, vertices[0][1] + dy * inset, vertices[0][2] + dz * inset },
          new float[] { vertices[1][0] - dx * inset, vertices[1][1] + dy * inset, vertices[1][2] - dz * inset },
          new float[] { vertices[2][0] - dx * inset, vertices[2][1] - dy * inset, vertices[2][2] - dz * inset },
          new float[] { vertices[3][0] + dx * inset, vertices[3][1] - dy * inset, vertices[3][2] + dz * inset },
        };
    }
}

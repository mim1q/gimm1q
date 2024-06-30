package dev.mim1q.gimm1q.client.render;

import net.minecraft.client.render.VertexConsumer;

/**
 * A wrapper for {@link VertexConsumer} that can be extended to modify the wrapped consumer's functionality,
 */
public class WrapperVertexConsumer implements VertexConsumer {
    protected VertexConsumer wrapped;

    protected WrapperVertexConsumer(VertexConsumer wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        return wrapped.vertex(x, y, z);
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        return wrapped.color(red, green, blue, alpha);
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        return wrapped.texture(u, v);
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        return wrapped.overlay(u, v);
    }

    @Override
    public VertexConsumer light(int u, int v) {
        return wrapped.light(u, v);
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        return wrapped.normal(x, y, z);
    }

    @Override
    public void next() {
        wrapped.next();
    }

    @Override
    public void fixedColor(int red, int green, int blue, int alpha) {
        wrapped.fixedColor(red, green, blue, alpha);
    }

    @Override
    public void unfixColor() {
        wrapped.unfixColor();
    }
}

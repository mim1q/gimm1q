package dev.mim1q.gimm1q.client.render.overlay;

import dev.mim1q.gimm1q.client.render.WrapperVertexConsumer;
import net.minecraft.client.render.VertexConsumer;
import org.jetbrains.annotations.ApiStatus;

/**
 * Custom type of {@link WrapperVertexConsumer} that is configured to render as a sort of "overlay" over ModelPart
 * Cuboids.
 * <br>
 * This can be used to render a texture on top of the original model, which is useful for any kind of effects that
 * should wrap an entity with something.
 * <br>
 * It can also be used to render an inverted wrapping around a model, which makes it look like an outline.
 * <br>
 * The overlay expects a 16x16 texture by default, has an offset of 1, is not inverted, doesn't skip planes and has
 * no animations.
 */
public class ModelOverlayVertexConsumer extends WrapperVertexConsumer {
    private float offset = 1f;
    private boolean inverted = false;
    private boolean skipPlanes = false;
    private int textureSize = 16;
    private OverlayUvMapper uvMapper = OverlayUvMapper.identity();

    protected ModelOverlayVertexConsumer(VertexConsumer wrapped) {
        super(wrapped);
    }

    /**
     * Sets the offset (distance from the surface of the original cuboid to the surface of the overlaying cuboid)
     *
     * @param offset the offset
     * @return this
     */
    public ModelOverlayVertexConsumer offset(float offset) {
        this.offset = offset;
        return this;
    }

    /**
     * Tells the renderer that the faces' normals and vertex order should be inverted. This can be used to render
     * a sort of outline around the original model.
     *
     * @return this
     */
    public ModelOverlayVertexConsumer inverted() {
        this.inverted = true;
        return this;
    }

    /**
     * Tells the renderer that cuboids which are flat shouldn't have an overlay applied
     *
     * @return this
     */
    public ModelOverlayVertexConsumer skipPlanes() {
        this.skipPlanes = true;
        return this;
    }

    /**
     * Sets the expected texture size. The te
     *
     * @param size the expected texture size
     * @return this
     */
    public ModelOverlayVertexConsumer textureSize(int size) {
        this.textureSize = size;
        return this;
    }

    /**
     * Sets the {@link OverlayUvMapper} to use. This can be used to animate the overlay (with a frame animation, or
     * by scrolling the texture)
     *
     * @return this
     */
    public ModelOverlayVertexConsumer mapUv(OverlayUvMapper uvMapper) {
        this.uvMapper = uvMapper;
        return this;
    }

    @ApiStatus.Internal
    public float getOffset() {
        return offset;
    }

    @ApiStatus.Internal
    public boolean isInverted() {
        return inverted;
    }

    @ApiStatus.Internal
    public boolean shouldSkipPlanes() {
        return skipPlanes;
    }

    @ApiStatus.Internal
    public int getTextureSize() {
        return textureSize;
    }

    @ApiStatus.Internal
    public float[] applyUvMapper(float u, float v, OverlayUvMapper.OverlayUvMapperQuadContext context) {
        return uvMapper.apply(u, v, context);
    }

    /**
     * Creates a new {@link ModelOverlayVertexConsumer} with the given {@link VertexConsumer} wrapped inside
     *
     * @param wrapped the wrapped {@link VertexConsumer}
     * @return a new {@link ModelOverlayVertexConsumer}
     */
    public static ModelOverlayVertexConsumer of(VertexConsumer wrapped) {
        return new ModelOverlayVertexConsumer(wrapped);
    }
}

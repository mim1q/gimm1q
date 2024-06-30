package dev.mim1q.gimm1q.client.render.overlay;

import org.jetbrains.annotations.ApiStatus;

/**
 * An interface that can be used to transform the UV coordinates of the overlay based on the animation progress
 * and vertex index.
 */
@FunctionalInterface
public interface OverlayUvMapper {
    float[] apply(float u, float v, OverlayUvMapperQuadContext context);

    /**
     * @return A mapper that returns the original, unmodified UV coordinates
     */
    static OverlayUvMapper identity() {
        return (u, v, context) -> new float[]{u, v};
    }

    /**
     * Experimental - may or may not look good. Use with caution, and prefer large textures.
     *
     * @param frameCount    The number of frames in the animation
     * @param frameDuration The duration of each frame, in ticks
     * @return A mapper returning a modified V coordinate based on the elapsed time to create a frame animation
     */
    @ApiStatus.Experimental
    static OverlayUvMapper frameAnimation(int frameCount, float frameDuration) {
        var frameHeight = 1f / frameCount;

        return (u, v, context) -> new float[]{
            u,
            (v / frameCount) + (frameHeight) * ((int) (context.animationProgress / frameDuration)) % frameCount
        };
    }

    /**
     * @param scrollSpeed The speed of the scroll animation, in V per tick. Set to 0.05f to scroll the entire
     *                    texture once per second.
     * @return A mapper returning a modified V coordinate based on the elapsed time to create a vertically scrolling
     * animation
     */
    static OverlayUvMapper verticalScrollAnimation(float scrollSpeed) {
        return (u, v, context) -> new float[]{
            u,
            v + (scrollSpeed * context.animationProgress) % 1
        };
    }

    /**
     * @param scrollSpeed The speed of the scroll animation, in U per tick. Set to 0.05f to scroll the entire
     *                    texture once per second.
     * @return A mapper returning a modified U coordinate based on the elapsed time to create a horizontally scrolling
     * animation
     */
    static OverlayUvMapper horizontalScrollAnimation(float scrollSpeed) {
        return (u, v, context) -> new float[]{
            u + (scrollSpeed * context.animationProgress * (context.isMirrored ? -1 : 1)) % 1,
            v
        };
    }

    /**
     * @param horizontalScrollSpeed The speed of the horizontal scroll animation, in U per tick.
     * @param verticalScrollSpeed   The speed of the vertical scroll animation, in V per tick.
     * @return A mapper returning modified U and V coordinates based on the elapsed time to create a diagonally
     * scrolling animation
     */
    static OverlayUvMapper diagonalScrollAnimation(float horizontalScrollSpeed, float verticalScrollSpeed) {
        return (u, v, context) -> new float[]{
            u + (horizontalScrollSpeed * context.animationProgress * (context.isMirrored ? -1 : 1)) % 1,
            v + (verticalScrollSpeed * context.animationProgress) % 1
        };
    }

    record OverlayUvMapperQuadContext(
        float animationProgress,
        int vertexIndex,
        boolean isMirrored
    ) {}
}

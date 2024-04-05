package dev.mim1q.gimm1q.client.highlight.crosshair;

import net.minecraft.util.Identifier;

/**
 * Interface for drawing a "tip" texture at the crosshair's position. Should only be used when provided by implementing
 * {@link CrosshairTipDrawerCallback#drawCrosshairTip}
 *
 * @see CrosshairTipDrawerCallback
 */
public interface CrosshairTipDrawer {
    /**
     * Draw a texture, centered at the crosshair's position and then offset by the given values.
     *
     * @param xOffset The horizontal offset of the tip
     * @param yOffset The vertical offset of the tip
     * @param size    The width and height of the tip
     * @param texture The texture to use for the tip (should be square)
     */
    void drawCrosshairTip(int xOffset, int yOffset, int size, Identifier texture);

    /**
     * Draw a 16x16 texture centered at the crosshair's position and then offset by the given values.
     *
     * @param xOffset The horizontal offset of the tip
     * @param yOffset The vertical offset of the tip
     * @param texture The texture to use for the tip (should be square and 16x16 pixels)
     *
     * @see #drawCrosshairTip(int, int, int, Identifier)
     */
    default void drawCrosshairTip(int xOffset, int yOffset, Identifier texture) {
        drawCrosshairTip(xOffset, yOffset, 16, texture);
    }

    /**
     * Draw a 16x16 texture centered at the crosshair's position.
     *
     * @param texture The texture to use for the tip (should be square and 16x16 pixels)
     */
    default void drawCrosshairTip(Identifier texture) {
        drawCrosshairTip(16, 0, 16, texture);
    }
}

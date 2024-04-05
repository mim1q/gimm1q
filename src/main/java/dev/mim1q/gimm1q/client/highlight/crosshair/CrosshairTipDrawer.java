package dev.mim1q.gimm1q.client.highlight.crosshair;

import net.minecraft.util.Identifier;

public interface CrosshairTipDrawer {
    void drawCrosshairTip(int xOffset, int yOffset, int size, Identifier texture);

    default void drawCrosshairTip(int xOffset, int yOffset, Identifier texture) {
        drawCrosshairTip(xOffset, yOffset, 16, texture);
    }

    default void drawCrosshairTip(Identifier texture) {
        drawCrosshairTip(16, 0, 16, texture);
    }
}

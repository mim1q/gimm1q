package dev.mim1q.gimm1q.client.highlight.gui;

import dev.mim1q.gimm1q.Gimm1q;
import net.minecraft.util.Identifier;

public interface GuiHighlightDrawer {
    Identifier DEFAULT_TEXTURE = Gimm1q.id("textures/gui/highlight.png");

    void highlightItem(int offsetX, int offsetY, int size, int argb, Identifier texture);

    default void highlightItem(int offsetX, int offsetY, int size, Identifier texture) {
        highlightItem(offsetX, offsetY, size, 0xFFFFFFFF, texture);
    }
    
    default void highlightItem(int argb, Identifier texture) {
        highlightItem(0, 0, 32, argb, texture);
    }

    default void highlightItem(int argb) {
        highlightItem(0, 0, 32, argb, DEFAULT_TEXTURE);
    }
}

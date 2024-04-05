package dev.mim1q.gimm1q.client.highlight.gui;

import dev.mim1q.gimm1q.Gimm1q;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * Interface for highlighting items in the GUI. Should only be used when provided by implementing
 * {@link GuiHighlightDrawerCallback#drawHighlights}
 *
 * @see GuiHighlightDrawerCallback
 */
@Environment(EnvType.CLIENT)
public interface GuiHighlightDrawer {
    /**
     * The default texture used for highlighting items. A simple white outline with a transparent center.
     */
    Identifier DEFAULT_TEXTURE = Gimm1q.id("textures/gui/highlight.png");

    /**
     * Highlights the currently considered {@link ItemStack} in the GUI. The offset is applied before scaling
     * the texture to the given size.
     *
     * @param offsetX The horizontal offset of the highlight
     * @param offsetY The vertical offset of the highlight
     * @param size    The width and height of the highlight
     * @param argb    The color of the highlight in ARGB format (0xAARRGGBB)
     * @param texture The texture to use for the highlight
     */
    void highlightItem(int offsetX, int offsetY, int size, int argb, Identifier texture);

    /**
     * Highlights the currently considered {@link ItemStack} in the GUI. Uses the original color of the provided
     * texture.
     *
     * @param offsetX The horizontal offset of the highlight
     * @param offsetY The vertical offset of the highlight
     * @param size    The width and height of the highlight
     * @param texture The texture to use for the highlight
     *
     * @see #highlightItem(int, int, int, int, Identifier)
     */
    default void highlightItem(int offsetX, int offsetY, int size, Identifier texture) {
        highlightItem(offsetX, offsetY, size, 0xFFFFFFFF, texture);
    }

    /**
     * Highlights the currently considered {@link ItemStack} in the GUI. Assumes the default highlight texture size
     * of 32x32 pixels and no offset.
     *
     * @param argb    The color of the highlight in ARGB format (0xAARRGGBB)
     * @param texture The texture to use for the highlight
     *
     * @see #highlightItem(int, int, int, int, Identifier)
     */
    default void highlightItem(int argb, Identifier texture) {
        highlightItem(0, 0, 32, argb, texture);
    }

    /**
     * Highlights the currently considered {@link ItemStack} in the GUI, using the default highlight texture.
     *
     * @param argb The color of the highlight in ARGB format (0xAARRGGBB)
     *
     * @see #highlightItem(int, int, int, int, Identifier)
     */
    default void highlightItem(int argb) {
        highlightItem(0, 0, 32, argb, DEFAULT_TEXTURE);
    }
}

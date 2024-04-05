package dev.mim1q.gimm1q.client.highlight.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * Callback for drawing highlights on items in the GUI.
 *
 * <p>
 * Register a callback using {@link #register(GuiHighlightDrawerCallback)}, and it will be called every frame to draw
 * highlights on items in the GUI. The callback should draw highlights using the provided {@link GuiHighlightDrawer}
 * interface. A {@link GuiHighlightDrawerContext context} is provided to the callback, containing data about the player
 * and the item stack currently being considered. An example of a highlight drawing callback is shown below.
 * <pre>{@code
 *     GuiHighlightDrawerCallback.register((drawer, context) -> {
 *         var stack = context.stack;
 *         if (stack.isIn(HIGHLIGHTED_ITEMS_TAG)) {
 *             // Highlight the item with a custom texture, using a magenta color
 *             drawer.highlightItem(0, 0, 32, 0xFFFF00FF, CUSTOM_HIGHLIGHT_TEXTURE);
 *         }
 *     }
 * }</pre>
 * </p>
 */
@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface GuiHighlightDrawerCallback {
    Event<GuiHighlightDrawerCallback> EVENT = EventFactory.createArrayBacked(
        GuiHighlightDrawerCallback.class,
        (listeners) -> (drawer, context) -> {
            for (GuiHighlightDrawerCallback listener : listeners) {
                listener.drawHighlights(drawer, context);
            }
        }
    );

    /**
     * Draw highlights on items in the GUI.
     *
     * @param drawer  The drawing interface for highlights
     * @param context The context for drawing highlights, containing the current player and the item stack
     * @see GuiHighlightDrawer
     */
    void drawHighlights(GuiHighlightDrawer drawer, GuiHighlightDrawerContext context);

    record GuiHighlightDrawerContext(
        PlayerEntity player,
        ItemStack stack
    ) {
    }

    /**
     * Register a highlight drawing callback.
     *
     * @param callback The callback to register
     * @see GuiHighlightDrawerCallback
     */
    static void register(GuiHighlightDrawerCallback callback) {
        EVENT.register(callback);
    }
}

package dev.mim1q.gimm1q.client.highlight;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * Callback for drawing highlights in the world.
 *
 * <p>
 * Register a callback using {@link #register}, and it will be called every frame to draw highlights in the world.
 * The callback should draw highlights using the provided {@link HighlightDrawer} interface.
 * <br>
 * For example, to draw a green highlight around the block under a player's feet:
 * </p>
 * <pre>{@code
 *     HighlightDrawerCallback.register((drawer, context) -> {
 *         var player = context.player;
 *         var pos = player.getBlockPos().down();
 *         drawer.highlightBlock(pos, 0xFF00FF00); // Green in ARGB format
 *     });
 * }</pre>
 *
 * @author Mim1q
 * @see #drawHighlights(HighlightDrawer, HighlightDrawerContext)
 */
@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface HighlightDrawerCallback {
    Event<HighlightDrawerCallback> EVENT = EventFactory.createArrayBacked(
        HighlightDrawerCallback.class,
        (listeners) -> (drawer, context) -> {
            for (HighlightDrawerCallback listener : listeners) {
                listener.drawHighlights(drawer, context);
            }
        }
    );

    /**
     * Draw highlights in the world.
     *
     * @param drawer  The drawing interface for highlights
     * @param context The context for drawing highlights, containing the current client player
     * @see HighlightDrawer
     */
    void drawHighlights(HighlightDrawer drawer, HighlightDrawerContext context);

    record HighlightDrawerContext(
        ClientPlayerEntity player
    ) {
    }

    /**
     * Register a highlight drawing callback.
     *
     * @param callback The callback to register
     * @see HighlightDrawerCallback
     */
    static void register(HighlightDrawerCallback callback) {
        EVENT.register(callback);
    }
}

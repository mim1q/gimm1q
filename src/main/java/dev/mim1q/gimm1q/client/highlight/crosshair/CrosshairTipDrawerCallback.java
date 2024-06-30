package dev.mim1q.gimm1q.client.highlight.crosshair;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Callback for drawing a "tip" texture at the crosshair's position.
 *
 * <p>
 * Register a callback using {@link #register(CrosshairTipDrawerCallback)}, and it will be called every frame to draw
 * the given texture next to / on top of the crosshair. The callback should draw the tip using the provided
 * {@link CrosshairTipDrawer} interface. A {@link CrosshairTipDrawerContext context} is provided to the callback.
 * A simple example of how to use this callback is shown below.
 * </p>
 * <pre>{@code
 *     CrosshairTipDrawerCallback.register((drawer, context) -> {
 *         var player = context.player();
 *         if (player.isSneaking()) {
 *             // Draw a custom tip texture, offset by 16 pixels to the right
 *             drawer.drawCrosshairTip(16, 0, PLAYER_SNEAKING_CROSSHAIR_TIP);
 *         }
 *     });
 * }</pre>
 */
@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface CrosshairTipDrawerCallback {
    Event<CrosshairTipDrawerCallback> EVENT = EventFactory.createArrayBacked(
        CrosshairTipDrawerCallback.class,
        (listeners) -> (drawer, context) -> {
            for (CrosshairTipDrawerCallback listener : listeners) {
                listener.drawCrosshairTip(drawer, context);
            }
        }
    );

    /**
     * Draw a "tip" texture at the crosshair's position.
     *
     * @param drawer  The drawing interface for the tip
     * @param context The context for drawing the tip, containing the current player
     * @see CrosshairTipDrawer
     */
    void drawCrosshairTip(CrosshairTipDrawer drawer, CrosshairTipDrawerContext context);

    /**
     * Register a crosshair tip drawing callback.
     *
     * @param callback The callback to register
     * @see CrosshairTipDrawerCallback
     */
    static void register(CrosshairTipDrawerCallback callback) {
        EVENT.register(callback);
    }

    record CrosshairTipDrawerContext(
        PlayerEntity player
    ) {
    }
}

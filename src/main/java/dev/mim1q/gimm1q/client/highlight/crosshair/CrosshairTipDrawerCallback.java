package dev.mim1q.gimm1q.client.highlight.crosshair;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;

public interface CrosshairTipDrawerCallback {
    Event<CrosshairTipDrawerCallback> EVENT = EventFactory.createArrayBacked(
        CrosshairTipDrawerCallback.class,
        (listeners) -> (drawer, player) -> {
            for (CrosshairTipDrawerCallback listener : listeners) {
                listener.drawCrosshairTip(drawer, player);
            }
        }
    );

    void drawCrosshairTip(CrosshairTipDrawer drawer, CrosshairTipDrawerContext context);

    static void register(CrosshairTipDrawerCallback callback) {
        EVENT.register(callback);
    }

    record CrosshairTipDrawerContext(
        PlayerEntity player
    ) {}
}

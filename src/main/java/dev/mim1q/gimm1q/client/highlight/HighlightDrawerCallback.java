package dev.mim1q.gimm1q.client.highlight;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.network.ClientPlayerEntity;

public interface HighlightDrawerCallback {
    Event<HighlightDrawerCallback> EVENT = EventFactory.createArrayBacked(HighlightDrawerCallback.class,
        (listeners) -> (drawer, context) -> {
            for (HighlightDrawerCallback listener : listeners) {
                listener.drawHighlights(drawer, context);
            }
        }
    );

    void drawHighlights(HighlightDrawer drawer, HighlightDrawerContext context);

    record HighlightDrawerContext(
        ClientPlayerEntity player
    ) {}
}

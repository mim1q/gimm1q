package dev.mim1q.gimm1q.client.highlight.gui;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface GuiHighlightDrawerCallback {
    Event<GuiHighlightDrawerCallback> EVENT = EventFactory.createArrayBacked(
        GuiHighlightDrawerCallback.class,
        (listeners) -> (drawer, context) -> {
            for (GuiHighlightDrawerCallback listener : listeners) {
                listener.drawHighlights(drawer, context);
            }
        }
    );

    void drawHighlights(GuiHighlightDrawer drawer, GuiHighlightDrawerContext context);

    record GuiHighlightDrawerContext(
        PlayerEntity player,
        ItemStack stack
    ) {
    }
}

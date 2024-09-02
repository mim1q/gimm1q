package dev.mim1q.gimm1q.valuecalculators;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.Identifier;

import java.util.Set;

public interface ValueCalculatorsReloadedCallback {
    Event<ValueCalculatorsReloadedCallback> EVENT = EventFactory.createArrayBacked(
        ValueCalculatorsReloadedCallback.class,
        listeners -> (ids) -> {
            for (ValueCalculatorsReloadedCallback listener : listeners) {
                listener.onValueCalculatorsReloaded(ids);
            }
        }
    );

    void onValueCalculatorsReloaded(Set<Identifier> ids);
}

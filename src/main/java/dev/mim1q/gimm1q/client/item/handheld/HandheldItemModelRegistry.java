package dev.mim1q.gimm1q.client.item.handheld;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public interface HandheldItemModelRegistry {
    default void register(Item item) {
        var itemId = Registries.ITEM.getId(item);
        var handheldId = new Identifier(itemId.getNamespace(), itemId.getPath() + "_handheld");
        register(item, itemId, handheldId);
    }

    default void register(Item item, Identifier handheldId) {
        var itemId = Registries.ITEM.getId(item);
        register(item, itemId, handheldId);
    }

    void register(Item item, Identifier modelId, Identifier handheldId);

    static HandheldItemModelRegistry getInstance() {
        return HandheldItemModelRegistryImpl.INSTANCE;
    }
}

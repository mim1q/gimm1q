package dev.mim1q.gimm1q.client.item.handheld;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * Registry for handheld item models.
 * <p>
 * Example usage:
 * <pre>{@code
 * HandheldItemModelRegistry.getInstance().register(
 *     SOME_ITEM,
 *     new Identifier("modid", "gui/some_item"),
 *     new Identifier("modid", "handheld/some_item")
 * );
 * }</pre>
 * <p>
 * This will register the {@code item/gui/some_item.json} model for displaying the item in the GUI, on the ground, and
 * in item frames, and the {@code item/handheld/some_item.json} model for displaying the item when held in the player's
 * hand.
 * </p>
 *
 * <p>
 * Alternatively, you can use {@link #register(Item, Identifier)} to register a custom handheld model and keep the
 * default model as the GUI one, or {@link #register(Item)} to use the default item model for the GUI model, and the
 * corresponding model suffixed with {@code _handheld} for the handheld model.
 * </p>
 */
@Environment(EnvType.CLIENT)
public interface HandheldItemModelRegistry {
    /**
     * Register a handheld item model for an Item. Assumes the default model ID for the GUI model, and will use the
     * model suffixed with {@code _handheld} for the handheld model
     *
     * @param item The item to register the model for
     * @see #register(Item, Identifier, Identifier)
     */
    default void register(Item item) {
        var itemId = Registries.ITEM.getId(item);
        var handheldId = new Identifier(itemId.getNamespace(), itemId.getPath() + "_handheld");
        register(item, itemId, handheldId);
    }

    /**
     * Register a handheld item model for an Item. Assumes the default model ID for the GUI model
     *
     * @param item       The item to register the model for
     * @param handheldId The handheld model ID for the item (relative to {@code models/item})
     * @see #register(Item, Identifier, Identifier)
     */
    default void register(Item item, Identifier handheldId) {
        var itemId = Registries.ITEM.getId(item);
        register(item, itemId, handheldId);
    }

    /**
     * Register a handheld item model for an Item. The Item will display the {@code modelId} in the GUI, on the ground,
     * and in item frames. The {@code handheldId} model will be used when the Item is held in the player's hand, both
     * in first and third person.
     *
     * @param item       The item to register the model for
     * @param modelId    The GUI model ID for the item (relative to {@code models/item})
     * @param handheldId The handheld model ID for the item (relative to {@code models/item})
     */
    void register(Item item, Identifier modelId, Identifier handheldId);

    /**
     * @return The instance of the handheld item model registry
     */
    static HandheldItemModelRegistry getInstance() {
        return HandheldItemModelRegistryImpl.INSTANCE;
    }
}

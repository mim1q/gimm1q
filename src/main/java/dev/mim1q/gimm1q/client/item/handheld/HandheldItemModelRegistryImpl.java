package dev.mim1q.gimm1q.client.item.handheld;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public final class HandheldItemModelRegistryImpl implements HandheldItemModelRegistry {
    public static final HandheldItemModelRegistry INSTANCE = new HandheldItemModelRegistryImpl();
    public static final Map<Item, Pair<ModelIdentifier, ModelIdentifier>> MODELS = new HashMap<>();

    @Override
    public void register(Item item, Identifier modelId, Identifier handheldId) {
        var id = new ModelIdentifier(modelId, "inventory");
        var handheld = new ModelIdentifier(handheldId, "inventory");
        ModelLoadingPlugin.register(context -> context.addModels(handheld, id));
        MODELS.put(item, new Pair<>(id, handheld));
    }
}

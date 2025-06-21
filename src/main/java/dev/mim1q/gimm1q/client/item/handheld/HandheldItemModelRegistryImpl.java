package dev.mim1q.gimm1q.client.item.handheld;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class HandheldItemModelRegistryImpl implements HandheldItemModelRegistry {
    public static final HandheldItemModelRegistry INSTANCE = new HandheldItemModelRegistryImpl();
    public static final Map<Item, Pair<Identifier, Identifier>> MODELS = new HashMap<>();

    @Override
    public void register(Item item, Identifier modelId, Identifier handheldId) {
        var prefixedModelId = modelId.withPrefixedPath("item/");
        var prefixedHandheldId = handheldId.withPrefixedPath("item/");
        ModelLoadingPlugin.register(context -> context.addModels(
            prefixedModelId,
            prefixedHandheldId
        ));
        MODELS.put(item, new Pair<>(prefixedModelId, prefixedHandheldId));
    }
}

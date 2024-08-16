package dev.mim1q.gimm1q.client.tooltip;

import net.minecraft.item.Item;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;

@ApiStatus.Internal
public class TooltipResolverRegistryImpl implements TooltipResolverRegistry {
    public static final HashMap<Item, TooltipResolver> resolvers = new HashMap<>();

    static TooltipResolverRegistry INSTANCE = new TooltipResolverRegistryImpl();

    @Override
    public void register(TooltipResolver resolver, Item... items) {
        for (Item item : items) {
            resolvers.put(item, resolver);
        }
    }

    @Override
    public TooltipResolver getResolver(Item item) {
        return resolvers.get(item);
    }
}

package dev.mim1q.gimm1q.client.tooltip;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface TooltipResolverRegistry {

    void register(TooltipResolver resolver, Item... items);

    @Nullable
    TooltipResolver getResolver(Item item);

    static TooltipResolverRegistry getInstance() {
        return TooltipResolverRegistryImpl.INSTANCE;
    }

    @FunctionalInterface
    interface TooltipResolver {
        void resolve(TooltipResolverContext context, TooltipHelper helper);
    }

    @ApiStatus.NonExtendable
    interface TooltipHelper {
        TooltipHelper add(Text text, boolean hidden);

        TooltipHelper hideSections(ItemStack.TooltipSection... sections);

        default TooltipHelper add(Text text) {
            return add(text, false);
        }

        default TooltipHelper addHidden(Text text) {
            return add(text, true);
        }
    }

    record TooltipResolverContext(
        ItemStack item,
        PlayerEntity player,
        TooltipContext tooltipContext
    ) {
    }
}

package dev.mim1q.gimm1q.client.tooltip;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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
        TooltipHelper addLine(Text text, boolean extra);

        TooltipHelper hideSections(ItemStack.TooltipSection... sections);

        TooltipHelper defaultStyle(Style style);

        TooltipHelper maxLineWidth(int width);

        default TooltipHelper addLine(Text text) {
            return addLine(text, false);
        }

        default TooltipHelper addExtraLine(Text text) {
            return addLine(text, true);
        }

        static List<Text> splitTextIfExceeds(Text text, int characters) {
            if (text.getString().length() <= characters) {
                return List.of(text);
            }

            var result = new ArrayList<Text>();
            var style = text.getStyle();
            var content = text.getString();

            while (content.length() > characters) {
                var lastSpace = 0;
                for (var i = 0; i < characters; ++i) {
                    if (content.charAt(i) == ' ') {
                        lastSpace = i;
                    }
                }

                result.add(Text.literal(content.substring(0, lastSpace)).setStyle(style));
                content = content.substring(lastSpace + 1);
            }
            result.add(Text.literal(content).setStyle(style));
            return result;
        }
    }

    record TooltipResolverContext(
        ItemStack item,
        PlayerEntity player,
        TooltipContext tooltipContext
    ) {
    }
}

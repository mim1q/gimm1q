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

/**
 * Registry for resolving custom item tooltips.
 * These allow you to resolve tooltips only when they start being displayed instead of every frame.
 * Useful in case the tooltip data requires resource-heavy calculations.
 */
public interface TooltipResolverRegistry {
    /**
     * Register the Tooltip Resolver for given items
     *
     * @param resolver the tooltip resolver
     * @param items    the items that will use the resolver for their tooltip
     */
    void register(TooltipResolver resolver, Item... items);

    /**
     * Retrieve the resolver used for a given item
     *
     * @param item an item that may or may not have a registered Tooltip Resolver
     * @return the Tooltip Resolver assigned to the Item, or null if there isn't one
     */
    @Nullable
    TooltipResolver getResolver(Item item);

    /**
     * @return the singleton instance of this registry
     */
    static TooltipResolverRegistry getInstance() {
        return TooltipResolverRegistryImpl.INSTANCE;
    }

    @FunctionalInterface
    interface TooltipResolver {
        /**
         * @param context the context containing some data that might be useful when resolving the tooltip
         * @param helper  an interface for performing actions on the tooltip
         * @see TooltipResolverContext
         * @see TooltipHelper
         */
        void resolve(TooltipResolverContext context, TooltipHelper helper);
    }

    /**
     * A set of useful actions that can be performed on an item tooltip
     */
    @ApiStatus.NonExtendable
    interface TooltipHelper {
        /**
         * Add a line (or multiple lines if it exceeds the max defined width) to the tooltip
         *
         * @param text  the text to add
         * @param extra if the line should only be shown when [Left Alt] is pressed
         * @return this
         */
        TooltipHelper addLine(Text text, boolean extra);

        /**
         * Lets you hide default sections of the tooltip
         *
         * @param sections the sections to hide
         * @return this
         */
        TooltipHelper hideSections(ItemStack.TooltipSection... sections);

        /**
         * The default style to apply to text if it doesn't have a custom one
         *
         * @param style the default style
         * @return this
         */
        TooltipHelper defaultStyle(Style style);

        /**
         * Sets the max width of the tooltip
         *
         * @param width the max width of a tooltip line, in characters
         * @return this
         */
        TooltipHelper maxLineWidth(int width);

        /**
         * Add a line to the tooltip
         *
         * @param text the text to add
         * @return this
         * @see TooltipHelper#addLine(Text, boolean)
         */
        default TooltipHelper addLine(Text text) {
            return addLine(text, false);
        }

        /**
         * Add a line to the tooltip that will only be shown when [Left Alt] is pressed
         *
         * @param text the text to add
         * @return this
         * @see TooltipHelper#addLine(Text, boolean)
         */
        default TooltipHelper addExtraLine(Text text) {
            return addLine(text, true);
        }

        /**
         * Splits a line of text into multiple lines if it exceeds the max defined width
         *
         * @param text       the text to split
         * @param characters the max number of characters per line
         * @return the list of lines that were created by splitting the original text
         */
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

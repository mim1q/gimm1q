package dev.mim1q.gimm1q.mixin.client.tooltip;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.mim1q.gimm1q.client.tooltip.TooltipResolverRegistry;
import dev.mim1q.gimm1q.client.tooltip.TooltipResolverRegistry.TooltipHelper;
import dev.mim1q.gimm1q.client.tooltip.TooltipResolverRegistry.TooltipResolverContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackTooltipMixin {
    @Unique
    private static List<Pair<Text, Boolean>> gimm1q$tooltipsToAdd = List.of();
    @Unique
    private long gimm1q$lastTooltipTime = 0;
    @Unique
    private static List<ItemStack.TooltipSection> gimm1q$hiddenTooltipSections = List.of();
    @Unique
    private static Style gimm1q$defaultStyle = Style.EMPTY;

    @Shadow
    public abstract Item getItem();

    @Inject(
        method = "getTooltip",
        at = @At("HEAD")
    )
    private void gimm1q$onGetTooltip(
        @Nullable PlayerEntity player,
        TooltipContext context,
        CallbackInfoReturnable<List<Text>> cir,
        @Share("tooltip") LocalRef<List<Text>> tooltip
    ) {
        if (player == null) return;
        var thisItemStack = (ItemStack) (Object) this;

        var tooltipResolver = TooltipResolverRegistry.getInstance().getResolver(this.getItem());
        if (tooltipResolver == null) {
            gimm1q$hiddenTooltipSections = List.of();
            return;
        }

        var currentTime = System.currentTimeMillis();
        final int[] maxLineWidth = {1024};

        final List<Pair<Text, Boolean>> currentTooltip;

        if (currentTime - gimm1q$lastTooltipTime <= 60) {
            currentTooltip = gimm1q$tooltipsToAdd;
        } else {
            currentTooltip = new ArrayList<>();

            gimm1q$hiddenTooltipSections = new ArrayList<>();
            tooltipResolver.resolve(
                new TooltipResolverContext(
                    thisItemStack,
                    player,
                    context
                ),
                new TooltipHelper() {
                    @Override
                    public TooltipHelper addLine(Text text, boolean extra) {
                        currentTooltip.add(new Pair<>(text, extra));
                        return this;
                    }

                    @Override
                    public TooltipHelper hideSections(ItemStack.TooltipSection... sections) {
                        gimm1q$hiddenTooltipSections.addAll(List.of(sections));
                        return this;
                    }

                    @Override
                    public TooltipHelper defaultStyle(Style style) {
                        gimm1q$defaultStyle = style;
                        return this;
                    }

                    @Override
                    public TooltipHelper maxLineWidth(int width) {
                        maxLineWidth[0] = width;
                        return this;
                    }
                }
            );

            var newCurrentTooltip = currentTooltip.stream().<Pair<Text, Boolean>>mapMulti((it, consumer) -> {
                var result = TooltipHelper.splitTextIfExceeds(it.getLeft(), maxLineWidth[0]);
                for (Text text : result) {
                    consumer.accept(new Pair<>(text, it.getRight()));
                }
            }).toList();

            currentTooltip.clear();
            currentTooltip.addAll(newCurrentTooltip);

            gimm1q$tooltipsToAdd = currentTooltip;
        }

        gimm1q$lastTooltipTime = currentTime;

        var newTooltip = new ArrayList<Text>();
        var altPressed = InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), InputUtil.GLFW_KEY_LEFT_ALT);
        var altTooltip = false;
        for (Pair<Text, Boolean> pair : currentTooltip) {
            if (!altPressed && pair.getRight()) {
                altTooltip = true;
                continue;
            }
            var text = pair.getLeft();
            if (text.getStyle().isEmpty()) {
                text = text.copy().setStyle(gimm1q$defaultStyle);
            }
            newTooltip.add(text);
        }
        if (!altPressed && altTooltip) {
            newTooltip.add(
                Text.translatable(
                    "tooltip.gimm1q.alt_tooltip",
                    Text.translatable("key.keyboard.left.alt").getString()
                ).formatted(Formatting.DARK_GRAY)
            );
        }
        tooltip.set(newTooltip);
    }

    @Inject(
        method = "getTooltip",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;hasNbt()Z",
            ordinal = 0
        )
    )
    private void gimm1q$onGetTooltip2(
        @Nullable PlayerEntity player,
        TooltipContext context,
        CallbackInfoReturnable<List<Text>> cir,
        @Local(ordinal = 0) List<Text> list,
        @Share("tooltip") LocalRef<List<Text>> tooltip
    ) {
        var newTooltip = tooltip.get();
        if (newTooltip == null || newTooltip.isEmpty()) return;
        list.addAll(tooltip.get());
    }

    @Inject(
        method = "getHideFlags()I",
        at = @At("RETURN"),
        cancellable = true
    )
    private void gimm1q$onGetHideFlags(CallbackInfoReturnable<Integer> cir) {
        if (gimm1q$hiddenTooltipSections.isEmpty()) return;

        var result = cir.getReturnValue();
        for (var section : gimm1q$hiddenTooltipSections) {
            result |= section.getFlag();
        }
        cir.setReturnValue(result);
    }
}

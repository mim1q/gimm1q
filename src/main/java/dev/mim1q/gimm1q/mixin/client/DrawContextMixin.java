package dev.mim1q.gimm1q.mixin.client;

import dev.mim1q.gimm1q.client.highlight.gui.GuiHighlightDrawerCallback;
import dev.mim1q.gimm1q.client.highlight.gui.GuiHighlightDrawerImpl;
import dev.mim1q.gimm1q.client.highlight.gui.GuiHighlightDrawerImpl.GuiHighlightDrawerContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ApiStatus.Internal
@Mixin(DrawContext.class)
public abstract class DrawContextMixin {
    @Shadow @Final private MatrixStack matrices;
    @Shadow
    abstract void drawTexturedQuad(Identifier texture, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, float red, float green, float blue, float alpha);

    @Inject(
        method = "drawItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;IIII)V",
        at = @At("TAIL")
    )
    private void gimm1q$injectDrawItem(
        LivingEntity entity,
        World world,
        ItemStack stack,
        int x,
        int y,
        int seed,
        int z,
        CallbackInfo ci
    ) {
        if (entity instanceof PlayerEntity player) {
            GuiHighlightDrawerImpl.setContext(
                new GuiHighlightDrawerContext(this::drawTexturedQuad, this.matrices, x, y)
            );

            GuiHighlightDrawerCallback.EVENT.invoker().drawHighlights(
                GuiHighlightDrawerImpl.INSTANCE,
                new GuiHighlightDrawerCallback.GuiHighlightDrawerContext(player, stack)
            );
        }
    }
}

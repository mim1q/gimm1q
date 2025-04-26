package dev.mim1q.gimm1q.mixin.client.model.feature;

import dev.mim1q.gimm1q.accessor.client.EntityModelVertexConsumerOverrideAccessor;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ApiStatus.Internal
@Mixin(FeatureRenderer.class)
public abstract class FeatureRendererMixin {
    @Inject(
        method = "renderModel",
        at = @At("RETURN")
    )
    private static <T extends LivingEntity> void gimm1q$renderModelOverlay(
        EntityModel<T> model,
        Identifier texture,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        T entity,
        int i,
        CallbackInfo ci
    ) {
        if (model == null) return;
        final var consumer = ((EntityModelVertexConsumerOverrideAccessor) model).gimm1q$getConsumerOverride(vertexConsumers);
        if (consumer == null) return;

        model.render(matrices, consumer, light, OverlayTexture.DEFAULT_UV, 0xFFFFFFFF);
    }
}

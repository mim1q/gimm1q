package dev.mim1q.gimm1q.mixin.client.model;

import dev.mim1q.gimm1q.accessor.client.EntityModelVertexConsumerOverrideAccessor;
import dev.mim1q.gimm1q.client.render.overlay.ModelOverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiFunction;

@Mixin(EntityModel.class)
public class EntityModelMixin<T extends Entity> implements EntityModelVertexConsumerOverrideAccessor {
    @Unique
    @Nullable
    private BiFunction<LivingEntity, VertexConsumerProvider, ModelOverlayVertexConsumer> gimm1q$consumerOverride = null;

    @Unique
    @Nullable
    private LivingEntity gimm1q$lastModelEntity = null;

    @Override
    public void gimm1q$setConsumerOverride(
        BiFunction<LivingEntity, VertexConsumerProvider, ModelOverlayVertexConsumer> vertexConsumerPicker,
        LivingEntity gimm1q$lastModelEntity
    ) {
        this.gimm1q$consumerOverride = vertexConsumerPicker;
        this.gimm1q$lastModelEntity = gimm1q$lastModelEntity;
    }

    @Override
    @Nullable
    public VertexConsumer gimm1q$getConsumerOverride(VertexConsumerProvider vertexConsumerProvider) {
        if (gimm1q$consumerOverride == null || gimm1q$lastModelEntity == null) return null;
        return gimm1q$consumerOverride.apply(gimm1q$lastModelEntity, vertexConsumerProvider);
    }

    @Inject(
        method = "copyStateTo",
        at = @At("HEAD")
    )
    private void gimm1q$copyStateTo(EntityModel<T> copy, CallbackInfo ci) {
        ((EntityModelVertexConsumerOverrideAccessor) copy).gimm1q$setConsumerOverride(gimm1q$consumerOverride, gimm1q$lastModelEntity);
    }
}

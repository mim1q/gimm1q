package dev.mim1q.gimm1q.mixin.client.item;

import dev.mim1q.gimm1q.client.item.handheld.HandheldItemModelRegistryImpl;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@ApiStatus.Internal
@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Shadow
    public abstract ItemModels getModels();

    @Shadow public abstract void renderItem(ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model);

    @Inject(
        method = "getModel(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)Lnet/minecraft/client/render/model/BakedModel;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void gimm1q$getModel(ItemStack stack, World world, LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> cir) {
        var model = HandheldItemModelRegistryImpl.MODELS.get(stack.getItem());
        if (model != null) {
            var bakedModel = getModels().getModelManager().getModel(model.getLeft());
            var override = bakedModel.getOverrides().apply(bakedModel, stack, (ClientWorld) world, entity, seed);
            cir.setReturnValue(override);
        }
    }

    @Inject(
        method = "renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/world/World;III)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void gimm1q$renderItem(
        LivingEntity entity,
        ItemStack item,
        ModelTransformationMode renderMode,
        boolean leftHanded,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        World world,
        int light,
        int overlay,
        int seed,
        CallbackInfo ci
    ) {
        if (item.isEmpty()) return;
        if (renderMode == ModelTransformationMode.FIRST_PERSON_RIGHT_HAND
            || renderMode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND
            || renderMode == ModelTransformationMode.THIRD_PERSON_RIGHT_HAND
            || renderMode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND
        ) {
            var model = HandheldItemModelRegistryImpl.MODELS.get(item.getItem());
            if (model != null) {
                var bakedModel = getModels().getModelManager().getModel(model.getRight());
                var override = bakedModel.getOverrides().apply(bakedModel, item, (ClientWorld) world, entity, seed);

                this.renderItem(item, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, override);
            }
            ci.cancel();
        }
    }
}

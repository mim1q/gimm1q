package dev.mim1q.gimm1q.mixin.client.item;

import dev.mim1q.gimm1q.client.item.handheld.HandheldItemModelRegistryImpl;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Shadow public abstract ItemModels getModels();

    @Inject(
        method = "getModel(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)Lnet/minecraft/client/render/model/BakedModel;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void gimm1q$getModel(ItemStack stack, World world, LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> cir) {
        var model = HandheldItemModelRegistryImpl.MODELS.get(stack.getItem());
        if (model != null) {
            cir.setReturnValue((getModels().getModelManager().getModel(model.getRight())));
        }
    }

    @ModifyVariable(
        method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;)V",
        at = @At("HEAD"),
        ordinal = 0,
        argsOnly = true
    )
    private BakedModel gimm1q$renderItemModifyModel(
        BakedModel originalModel, ItemStack stack, ModelTransformationMode mode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay
    ) {
        if (mode == ModelTransformationMode.GUI || mode == ModelTransformationMode.GROUND || mode == ModelTransformationMode.FIXED) {
            var model = HandheldItemModelRegistryImpl.MODELS.get(stack.getItem());
            if (model != null) return getModels().getModelManager().getModel(model.getLeft());
        }
        return originalModel;
    }
}

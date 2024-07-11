package dev.mim1q.gimm1q.mixin.entity;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mim1q.gimm1q.effect.ExtendedStatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Iterator;

@ApiStatus.Internal
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow
    public abstract ItemStack getActiveItem();

    @Unique
    private boolean gimm1q$preventRemovingCurrentEffect = false;

    @WrapOperation(
        method = "clearStatusEffects",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;onStatusEffectRemoved(Lnet/minecraft/entity/effect/StatusEffectInstance;)V"
        )
    )
    private void gimm1q$wrapOnStatusEffectRemoved(LivingEntity instance, StatusEffectInstance effect, Operation<Void> original) {
        if (
            getActiveItem().isOf(Items.MILK_BUCKET)
            && effect.getEffectType() instanceof ExtendedStatusEffect extendedEffect
            && !extendedEffect.canBeCuredWithMilk()
        ) {
            gimm1q$preventRemovingCurrentEffect = true;
            return;
        }
        original.call(instance, effect);
    }

    @WrapWithCondition(
        method = "clearStatusEffects",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Iterator;remove()V"
        )
    )
    private boolean gimm1q$wrapIteratorRemoveWithConditionInClearStatusEffects(Iterator<StatusEffectInstance> instance) {
        if (gimm1q$preventRemovingCurrentEffect) {
            gimm1q$preventRemovingCurrentEffect = false;
            return false;
        }
        return true;
    }
}

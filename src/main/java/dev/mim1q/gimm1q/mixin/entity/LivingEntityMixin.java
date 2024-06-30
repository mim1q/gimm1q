package dev.mim1q.gimm1q.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.mim1q.gimm1q.effect.ExtendedStatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow
    public abstract ItemStack getActiveItem();

    @WrapOperation(
        method = "clearStatusEffects",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Iterator;next()Ljava/lang/Object;"
        )
    )
    private Object gimm1q$wrapIteratorNextInClearStatusEffects(
        Iterator<StatusEffectInstance> instance,
        Operation<StatusEffectInstance> original
    ) {
        if (getActiveItem().isOf(Items.MILK_BUCKET)) {
            while (instance.hasNext()) {
                StatusEffectInstance next = instance.next();
                if (!(next.getEffectType() instanceof ExtendedStatusEffect effect && !effect.canBeCuredWithMilk())) {
                    return next;
                }
            }
            return null;
        }
        return original.call(instance);
    }

    @WrapOperation(
        method = "clearStatusEffects",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;onStatusEffectRemoved(Lnet/minecraft/entity/effect/StatusEffectInstance;)V"
        )
    )
    private void gimm1q$wrapClearStatusEffects(
        LivingEntity instance, StatusEffectInstance effect, Operation<Void> original
    ) {
        if (effect == null) return;
        original.call(instance, effect);
    }

    @Inject(
        method = "clearStatusEffects",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;onStatusEffectRemoved(Lnet/minecraft/entity/effect/StatusEffectInstance;)V",
            shift = At.Shift.AFTER
        ),
        cancellable = true
    )
    private void gimm1q$cancelClearStatusEffects(
        CallbackInfoReturnable<Boolean> cir,
        @Local Iterator<StatusEffectInstance> iterator,
        @Local boolean bl
    ) {
        if (!iterator.hasNext()) cir.setReturnValue(bl);
    }
}

package dev.mim1q.gimm1q.mixin.client;

import dev.mim1q.gimm1q.interfaces.ShakeableCameraAccessor;
import dev.mim1q.gimm1q.interpolation.AnimatedProperty;
import dev.mim1q.gimm1q.interpolation.Easing;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static net.minecraft.util.math.MathHelper.lerp;
import static net.minecraft.util.math.MathHelper.sign;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerCameraShakeMixin
    extends LivingEntity
    implements ShakeableCameraAccessor {


    @Unique private AnimatedProperty cameraShakeIntensity = new AnimatedProperty(0.0F);
    @Unique private int cameraShakeTicks = 0;
    @Unique private int cameraShakeDuration = 0;
    @Unique private float lastCameraShakePitch = 0.0f;
    @Unique private float cameraShakePitch = 0.0f;
    @Unique private float lastCameraShakeYaw = 0.0f;
    @Unique private float cameraShakeYaw = 0.0f;

    protected ClientPlayerCameraShakeMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void shakeCamera(float intensity, int duration) {
        this.cameraShakeTicks = 0;
        this.cameraShakeDuration = duration;
        this.cameraShakeIntensity.transitionTo(intensity, min(5, duration), Easing::easeInOutCubic);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void gimm1q$tick(CallbackInfo ci) {
        if (this.cameraShakeTicks <= this.cameraShakeDuration) {
            if (this.cameraShakeTicks == 5) {
                this.cameraShakeIntensity.transitionTo(
                    0.0f,
                    max(5, cameraShakeDuration - 5),
                    Easing::easeOutCubic
                );
            }
            this.cameraShakeTicks++;
        } else {
            this.cameraShakeIntensity.transitionTo(0.0f, 5, Easing::easeOutCubic);
        }

        var intensity = this.cameraShakeIntensity.update(this.age);

        this.lastCameraShakePitch = this.cameraShakePitch;
        this.cameraShakePitch = getNewRotation(this.random, 0) * intensity * 0.5f;
        this.lastCameraShakeYaw = this.cameraShakeYaw;
        this.cameraShakeYaw = getNewRotation(this.random, -sign(lastCameraShakeYaw)) * intensity;
    }

    @Inject(method = "getPitch", at = @At("HEAD"), cancellable = true)
    private void gimm1q$getPitch(float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (this.cameraShakeDuration > 0) {
            var pitch = lerp(tickDelta, this.prevPitch, this.getPitch());
            var addedPitch = easeShake(this.lastCameraShakePitch, this.cameraShakePitch, tickDelta);
            cir.setReturnValue(pitch + addedPitch);
        }
    }

    @Inject(method = "getYaw", at = @At("HEAD"), cancellable = true)
    private void gimm1q$getYaw(float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (this.cameraShakeDuration > 0) {
            var yaw = lerp(tickDelta, this.prevHeadYaw, this.headYaw);
            var addedYaw = easeShake(this.lastCameraShakeYaw, this.cameraShakeYaw, tickDelta);
            cir.setReturnValue(yaw + addedYaw);
        }
    }

    @Unique
    private static float getNewRotation(Random random, int sign) {
        var result = random.nextFloat();
        if (result < 0.5f) result += 0.5f;
        if (sign == 0) return result * (random.nextBoolean() ? 1 : -1);
        return result * sign;
    }

    @Unique
    private static float easeShake(float start, float end, float delta) {
        return Easing.easeInOutCubic(start, end, delta);
    }
}

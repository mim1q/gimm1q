package dev.mim1q.gimm1q.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.mim1q.gimm1q.interpolation.AnimatedProperty;
import dev.mim1q.gimm1q.interpolation.Easing;
import dev.mim1q.gimm1q.screenshake.ScreenShakeAccessor;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static net.minecraft.util.math.MathHelper.sign;

@ApiStatus.Internal
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerScreenShakeMixin extends LivingEntity implements ScreenShakeAccessor {
    @Unique
    private final AnimatedProperty cameraShakeIntensity = new AnimatedProperty(0.0F);
    @Unique
    private float lastIntensity = 0.0f;
    @Unique
    private int cameraShakeTicks = 0;
    @Unique
    private int cameraShakeDuration = 0;
    @Unique
    private float lastCameraShakePitch = 0.0f;
    @Unique
    private float cameraShakePitch = 0.0f;
    @Unique
    private float lastCameraShakeYaw = 0.0f;
    @Unique
    private float cameraShakeYaw = 0.0f;

    protected ClientPlayerScreenShakeMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void shakeCamera(float intensity, int duration) {
        if (cameraShakeTicks >= cameraShakeDuration || intensity >= lastIntensity) {
            this.lastIntensity = intensity;
            this.cameraShakeTicks = 0;
            this.cameraShakeDuration = duration;
            this.cameraShakeIntensity.transitionTo(intensity, min(5, duration), Easing::easeInOutCubic);
        }
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

    @ModifyReturnValue(
        method = "getPitch(F)F",
        at = @At("RETURN")
    )
    private float gimm1q$getPitch(float pitch, float tickDelta) {
        if (this.cameraShakeDuration > 0 && Math.abs(cameraShakePitch) > 0.0001f) {
            return pitch + easeShake(this.lastCameraShakePitch, this.cameraShakePitch, tickDelta);
        }
        return pitch;
    }

    @ModifyReturnValue(
        method = "getYaw(F)F",
        at = @At("RETURN")
    )
    private float gimm1q$getYaw(float yaw, float tickDelta) {
        if (this.cameraShakeDuration > 0 && Math.abs(cameraShakeYaw) > 0.0001f) {
            return yaw + easeShake(this.lastCameraShakeYaw, this.cameraShakeYaw, tickDelta);
        }
        return yaw;
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

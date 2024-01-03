package dev.mim1q.gimm1q.mixin.client;

import com.mojang.authlib.GameProfile;
import dev.mim1q.gimm1q.interfaces.ShakeableCameraAccessor;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerCameraShakeMixin
    extends PlayerEntity
    implements ShakeableCameraAccessor {

    public ClientPlayerCameraShakeMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Unique private float cameraShakeIntensity = 0.0f;
    @Unique private int cameraShakeDuration = 0;
    @Unique private int cameraLastShakeTick = 0;

    @Override
    public float getCameraShake() {
        return 0;
    }

    @Override
    public void shakeCamera(float intensity, int duration) {
        this.cameraLastShakeTick = this.age;
        this.cameraShakeDuration = duration;
        this.cameraShakeIntensity = intensity;
    }
}

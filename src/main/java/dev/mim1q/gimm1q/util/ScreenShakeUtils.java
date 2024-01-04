package dev.mim1q.gimm1q.util;

import dev.mim1q.gimm1q.network.s2c.CameraShakeS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class ScreenShakeUtils {
    public static void applyShake(ServerPlayerEntity player, float intensity, int duration) {
        new CameraShakeS2CPacket(intensity, duration).send(player);
    }

    public static void shakeAround(
        ServerWorld world,
        Vec3d center,
        float maxIntensity,
        int maxDuration,
        double innerRadius,
        double outerRadius
    ) {
        var innerRadiusSquared = innerRadius * innerRadius;
        var outerRadiusSquared = outerRadius * outerRadius;
        for (var player : world.getPlayers()) {
            var distanceSquared = player.getPos().squaredDistanceTo(center);
            if (distanceSquared > outerRadiusSquared) continue;
            if (distanceSquared < innerRadiusSquared) {
                applyShake(player, maxIntensity, maxDuration);
            } else {
                var power = 1 - (distanceSquared - innerRadiusSquared) / (outerRadiusSquared - innerRadiusSquared);
                applyShake(player, (float) (maxIntensity * power), maxDuration);
            }
        }
    }
}

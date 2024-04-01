package dev.mim1q.gimm1q.screenshake;

import dev.mim1q.gimm1q.network.s2c.CameraShakeS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

/**
 * Server-side utility functions for shaking players' screens. Should be your go-to class for screen shake effects.
 */
public class ScreenShakeUtils {
    /**
     * Shakes the provided player's screen, using a previously set intensity modifier.
     *
     * @param player       The player whose screen should be shaken.
     * @param intensity    The intensity of the shake. 1.0f is a good default value for most non-extreme cases.
     * @param duration     The duration of the shake in ticks.
     * @param modifierName The name of the modifier to apply to the shake intensity.
     * @see ScreenShakeModifiers
     */
    public static void applyShake(ServerPlayerEntity player, float intensity, int duration, String modifierName) {
        new CameraShakeS2CPacket(intensity, duration, modifierName).send(player);
    }

    /**
     * Shakes the provided player's screen, without applying any modifiers.
     *
     * @param player    The player whose screen should be shaken.
     * @param intensity The intensity of the shake. 1.0f is a good default value for most non-extreme cases.
     * @param duration  The duration of the shake in ticks.
     * @see #applyShake(ServerPlayerEntity, float, int, String)
     */
    public static void applyShake(ServerPlayerEntity player, float intensity, int duration) {
        applyShake(player, intensity, duration, "");
    }

    /**
     * Shakes the screens of players around a given center point, with the intensity diminishing with the square of the
     * distance. The distance "starts" at the {@code innerRadius}, with everyone within that radius of the center
     * getting the maximum intensity, decreasing until the {@code outerRadius}. Uses the provided {@code modifierName}
     * to modify the intensity of the screen shake.
     *
     * @param world        {@link ServerWorld} to shake players in.
     * @param center       The center {@link Vec3d} position of the screen shake event.
     * @param maxIntensity Intensity of the shake near the center.
     * @param maxDuration  The duration of the screen shake.
     * @param innerRadius  The inner radius, within which everyone will get the {@code maxIntensity} intensity shake.
     * @param outerRadius  The outer radius, outside which players won't have their screen shaken at all.
     * @param modifierName Name of the modifier to apply to the shake intensity.
     * @see ScreenShakeModifiers
     */
    public static void shakeAround(
        ServerWorld world,
        Vec3d center,
        float maxIntensity,
        int maxDuration,
        double innerRadius,
        double outerRadius,
        String modifierName
    ) {
        var innerRadiusSquared = innerRadius * innerRadius;
        var outerRadiusSquared = outerRadius * outerRadius;
        for (var player : world.getPlayers()) {
            var distanceSquared = player.getPos().squaredDistanceTo(center);
            if (distanceSquared > outerRadiusSquared) continue;
            if (distanceSquared < innerRadiusSquared) {
                applyShake(player, maxIntensity, maxDuration, modifierName);
            } else {
                var power = 1 - (distanceSquared - innerRadiusSquared) / (outerRadiusSquared - innerRadiusSquared);
                applyShake(player, (float) (maxIntensity * power), maxDuration, modifierName);
            }
        }
    }

    /**
     * Shakes the screens of players around a given center point, without using any intensity modifiers.
     *
     * @param world        {@link ServerWorld} to shake players in.
     * @param center       The center {@link Vec3d} position of the screen shake event.
     * @param maxIntensity Intensity of the shake near the center.
     * @param maxDuration  The duration of the screen shake.
     * @param innerRadius  The inner radius, within which everyone will get the {@code maxIntensity} intensity shake.
     * @param outerRadius  The outer radius, outside which player won't have their screen shaken at all.
     * @see #shakeAround(ServerWorld, Vec3d, float, int, double, double, String)
     */
    public static void shakeAround(
        ServerWorld world,
        Vec3d center,
        float maxIntensity,
        int maxDuration,
        double innerRadius,
        double outerRadius
    ) {
        shakeAround(world, center, maxIntensity, maxDuration, innerRadius, outerRadius, "");
    }
}

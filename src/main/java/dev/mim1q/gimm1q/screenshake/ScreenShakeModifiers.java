package dev.mim1q.gimm1q.screenshake;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * The screen shake "modifier" registry.
 * <p>
 * Should be used when initializing your mod to modify the intensity of various
 * screen shake events you may want to trigger.
 * </p>
 * <p>
 * You might want to load these modifiers from a config file or game rule to allow users to customize the screen shake
 * intensity to their preference. Example:
 *
 * <pre>{@code
 *     // After loading the modifiers from a config file
 *     ScreenShakeModifiers.setModifier("nuclear_explosion", CONFIG.nuclearExplosionModifier);
 * }</pre>
 * <p>
 * This will make all screen shake events triggered with the "nuclear_explosion" modifier through
 * {@link ScreenShakeUtils#applyShake(ServerPlayerEntity, float, int, String)} or
 * {@link ScreenShakeUtils#shakeAround(ServerWorld, Vec3d, float, int, double, double, String)} to have their intensity
 * multiplied by the configured value.
 * </p>
 */
public class ScreenShakeModifiers {
    private static final Map<String, Float> MODIFIERS = new HashMap<>();

    @ApiStatus.Internal
    public static float getModifier(String name) {
        return MODIFIERS.getOrDefault(name, 1f);
    }

    public static void setModifier(String name, float value) {
        if (name.isBlank()) throw new IllegalArgumentException("Modifier name cannot be blank");
        MODIFIERS.put(name, value);
    }
}

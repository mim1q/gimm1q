package dev.mim1q.gimm1q.screenshake;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Client-side accessor to make a {@link net.minecraft.client.network.ClientPlayerEntity}'s camera shake
 * <p>
 * Example use:
 * </p>
 * <pre>{@code
 *     var player = MinecraftClient.getInstance().player;
 *     if (player != null) {
 *         ((ScreenShakeAccessor) player).shakeCamera(1.0f, 20);
 *     }
 * }</pre>
 * <p>
 * Prefer the server-side {@link ScreenShakeUtils} for shaking players' screens, unless you <i>really</i> need it to be
 * called from the client-side.
 * </p>
 */
@Environment(EnvType.CLIENT)
public interface ScreenShakeAccessor {
    /**
     * Shakes the player's camera.
     *
     * @param intensity The intensity of the shake. 1.0f is a good default value for most non-extreme cases.
     * @param duration  The duration of the shake in ticks.
     */
    void shakeCamera(float intensity, int duration);
}

package dev.mim1q.gimm1q.interpolation;

import dev.mim1q.gimm1q.interpolation.AnimatedProperty.EasingFunction;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

/**
 * Utility functions related to easing
 */
public class EasingUtils {
    /**
     * Interpolates the individual values of two {@link Vec3d} objects and returns a new one with the given easing
     * function applied using the given {@code delta} value.
     *
     * @param start          the starting {@link Vec3d}
     * @param end            the ending {@link Vec3d}
     * @param delta          the delta value to use for interpolation
     * @param easingFunction the easing function to use for interpolation, see {@link EasingFunction}
     * @return a new {@link Vec3d} object with the interpolated values
     * @see Easing
     * @see EasingFunction
     */
    public static Vec3d interpolateVec(Vec3d start, Vec3d end, float delta, EasingFunction easingFunction) {
        return new Vec3d(
                easingFunction.ease((float) start.getX(), (float) end.getX(), delta),
                easingFunction.ease((float) start.getY(), (float) end.getY(), delta),
                easingFunction.ease((float) start.getZ(), (float) end.getZ(), delta)
        );
    }

    /**
     * Interpolates the individual values of two {@link Vector3f} objects and returns a new one with the given easing
     * function applied using the given {@code delta} value.
     *
     * @param start          the starting {@link Vector3f}
     * @param end            the ending {@link Vector3f}
     * @param delta          the delta value to use for interpolation
     * @param easingFunction the easing function to use for interpolation, see {@link EasingFunction}
     * @return a new {@link Vector3f} object with the interpolated values
     * @see Easing
     * @see EasingFunction
     */
    public static Vector3f interpolateVec(Vector3f start, Vector3f end, float delta, EasingFunction easingFunction) {
        return new Vector3f(
                easingFunction.ease(start.x, end.x, delta),
                easingFunction.ease(start.y, end.y, delta),
                easingFunction.ease(start.z, end.z, delta)
        );
    }
}

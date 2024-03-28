package dev.mim1q.gimm1q.interpolation;

import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public class EasingUtils {
    public static Vec3d interpolateVec(Vec3d start, Vec3d end, float delta, AnimatedProperty.EasingFunction easingFunction) {
        return new Vec3d(
            easingFunction.ease((float)start.getX(), (float)end.getX(), delta),
            easingFunction.ease((float)start.getY(), (float)end.getY(), delta),
            easingFunction.ease((float)start.getZ(), (float)end.getZ(), delta)
        );
    }

    public static Vector3f interpolateVec(Vector3f start, Vector3f end, float delta, AnimatedProperty.EasingFunction easingFunction) {
        return new Vector3f(
            easingFunction.ease(start.x, end.x, delta),
            easingFunction.ease(start.y, end.y, delta),
            easingFunction.ease(start.z, end.z, delta)
        );
    }
}

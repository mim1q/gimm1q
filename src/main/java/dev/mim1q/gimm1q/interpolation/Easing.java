package dev.mim1q.gimm1q.interpolation;

import net.minecraft.util.math.MathHelper;

import static net.minecraft.util.math.MathHelper.clamp;

// https://easings.net has been a great help in creating these functions :)
public class Easing {
    //#region Linear Interpolation
    public static float lerp(float start, float end, float delta) {
        return start + delta * (end - start);
    }

    public static float clampedLerp(float start, float end, float delta) {
        return lerp(start, end, clamp(delta, 0, 1));
    }

    public static float clampDelta(float delta) {
        return clamp(delta, 0, 1);
    }
    
    //#endregion

    //#region Easing Functions
    public static float easeInQuad(float start, float end, float delta) {
        return lerp(start, end, easeInQuadDelta(clampDelta(delta)));
    }

    public static float easeOutQuad(float start, float end, float delta) {
        return lerp(start, end, easeOutQuadDelta(clampDelta(delta)));
    }

    public static float easeInOutQuad(float start, float end, float delta) {
        return lerp(start, end, easeInOutQuadDelta(clampDelta(delta)));
    }

    public static float easeInCubic(float start, float end, float delta) {
        return lerp(start, end, easeInCubicDelta(clampDelta(delta)));
    }

    public static float easeOutCubic(float start, float end, float delta) {
        return lerp(start, end, easeOutCubicDelta(clampDelta(delta)));
    }

    public static float easeInOutCubic(float start, float end, float delta) {
        return lerp(start, end, easeInOutCubicDelta(clampDelta(delta)));
    }

    public static float easeInBack(float start, float end, float delta) {
        return lerp(start, end, easeInBackDelta(clampDelta(delta)));
    }

    public static float easeOutBack(float start, float end, float delta) {
        return lerp(start, end, easeOutBackDelta(clampDelta(delta)));
    }

    public static float easeInOutBack(float start, float end, float delta) {
        return lerp(start, end, easeInOutBackDelta(clampDelta(delta)));
    }

    public static float easeInElastic(float start, float end, float delta) {
        return lerp(start, end, easeInElasticDelta(clampDelta(delta)));
    }

    public static float easeOutElastic(float start, float end, float delta) {
        return lerp(start, end, easeOutElasticDelta(clampDelta(delta)));
    }

    public static float easeInOutElastic(float start, float end, float delta) {
        return lerp(start, end, easeInOutElasticDelta(clampDelta(delta)));
    }

    public static float easeOutBounce(float start, float end, float delta) {
        return lerp(start, end, easeOutBounce(clampDelta(delta)));
    }

    //#endregion

    //#region Delta Calculation for Easing Functions
    private static float easeInQuadDelta(float x) {
        return x * x;
    }

    private static float easeOutQuadDelta(float x) {
        return 1 - easeInQuadDelta(1 - x);
    }

    private static float easeInOutQuadDelta(float x) {
        if (x < 0.5f) return 2 * x * x;
        return 1 - (-2 * x + 2) * (-2 * x + 2) / 2;
    }

    private static float easeInCubicDelta(float x) {
        return x * x * x;
    }

    private static float easeOutCubicDelta(float x) {
        return 1 - easeInCubicDelta(1 - x);
    }

    private static float easeInOutCubicDelta(float x) {
        if (x < 0.5f)  return 4 * x * x * x;
        return 1 - (-2 * x + 2) * (-2 * x + 2) * (-2 * x + 2) / 2;
    }

    private static float easeInBackDelta(float x) {
        final var c1 = 1.70158f;
        final var c3 = c1 + 1;
        return c3 * x * x * x - c1 * x * x;
    }

    private static float easeOutBackDelta(float x) {
        final var c1 = 1.70158f;
        return 1 + (c1 + 1) * (x - 1) * (x - 1) * (x - 1) + c1 * (x - 1) * (x - 1);
    }
    
    private static float easeInOutBackDelta(float x) {
        final var c2 = 1.70158f * 1.525f;
        if (x < 0.5f) return 2 * x * (2 * x) * ((c2 + 1) * 2 * x - c2) / 2;
        return ((2 * x - 2) * (2 * x - 2) * ((c2 + 1) * (x * 2 - 2) + c2) + 2) / 2;
    }

    private static float easeInElasticDelta(float x) {
        final var c4 = (2 * MathHelper.PI) / 3;
        if (x == 0) return 0;
        if (x == 1) return 1;
        return (float) (-Math.pow(2, 10 * x - 10) * Math.sin((x * 10 - 10.75) * c4));
    }

    private static float easeOutElasticDelta(float x) {
        final var c4 = (2 * MathHelper.PI) / 3;
        if (x == 0) return 0;
        if (x == 1) return 1;
        return (float) (Math.pow(2, -10 * x) * Math.sin((x * 10 - 0.75) * c4) + 1);
    }

    private static float easeInOutElasticDelta(float x) {
        final var c5 = (2 * MathHelper.PI) / 4.5;

        if (x == 0) return 0;
        if (x == 1) return 1;

        final var sine = Math.sin((20 * x - 11.125) * c5);

        if (x < 0.5f) return (float) (-Math.pow(2, 20 * x - 10) * sine / 2);
        return (float) (Math.pow(2, -20 * x + 10) * sine / 2 + 1);
    }

    private static float easeOutBounce(float x) {
        final var n1 = 7.5625f;
        final var d1 = 2.75f;

        if (x < 1 / d1) return n1 * x * x;
        if (x < 2 / d1) return n1 * (x -= 1.5f / d1) * x + 0.75f;
        if (x < 2.5 / d1) return n1 * (x -= 2.25f / d1) * x + 0.9375f;
        return n1 * (x -= 2.625f / d1) * x + 0.984375f;
    }
    //#endregion
}

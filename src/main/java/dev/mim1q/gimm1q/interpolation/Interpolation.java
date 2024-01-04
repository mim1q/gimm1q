package dev.mim1q.gimm1q.interpolation;

// https://easings.net has been a great help in creating these functions :)
public class Interpolation {
    public static float lerp(float start, float end, float delta) {
        return start + delta * (end - start);
    }

    public static float easeInOutQuad(float start, float end, float delta) {
        if (delta < 0.5f) {
            return lerp(start, end, 2f * delta * delta);
        }
        var t = -2f * delta + 2f;
        return lerp(start, end, 1f - (t * t) / 2f);
    }

    public static float easeInOutCubic(float start, float end, float delta) {
        if (delta < 0.5f) {
            return lerp(start, end, 4f * delta * delta * delta);
        }
        var t = -2f * delta + 2f;
        return lerp(start, end, 1f - (t * t * t) / 2f);
    }

    public static float easeOutCubic(float start, float end, float delta) {
        var t = 1 - delta;
        return lerp(start, end, 1f - (t * t * t));
    }

    public static float easeOutElastic(float start, float end, float delta) {
        var c4 = (float) (2 * Math.PI) / 3;
        if (delta == 0) {
            return start;
        }
        if (delta == 1) {
            return end;
        }
        return lerp(
            start,
            end,
            (float) Math.pow(2, -10 * delta) * (float) Math.sin((delta * 10 - 0.75) * c4) + 1
        );
    }
}

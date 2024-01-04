package dev.mim1q.gimm1q.interpolation;

import org.joml.Math;

public class AnimatedProperty {
    private float value;
    private float lastValue;
    private float targetValue;
    private float lastTime = 0.0F;
    private float time = 0.0F;
    private float duration = 10.0F;
    private EasingFunction easingFunction;

    public AnimatedProperty(float value, EasingFunction easingFunction) {
        this.value = value;
        this.lastValue = value;
        this.targetValue = value;
        this.easingFunction = easingFunction;
    }

    public AnimatedProperty(float value) {
        this(value, Easing::easeInOutQuad);
    }

    public void transitionTo(float targetValue, float duration, EasingFunction easingFunction) {
        boolean result = this.transitionTo(targetValue, duration);
        if (result) {
            this.easingFunction = easingFunction;
        }
    }

    public boolean transitionTo(float targetValue, float duration) {
        if (targetValue == this.targetValue) {
            return false;
        }
        this.lastValue = this.value;
        this.lastTime = this.time;
        this.duration = duration;
        this.targetValue = targetValue;
        return true;
    }

    public float update(float time) {
        this.time = time;
        this.value = this.easingFunction.ease(this.lastValue, this.targetValue, this.getProgress());
        return getValue();
    }

    public float getProgress() {
        return Math.clamp(0.0F, 1.0F, (this.time - this.lastTime) / this.duration);
    }

    public float getValue() {
        return this.value;
    }

    @FunctionalInterface
    public interface EasingFunction {
        float ease(float start, float end, float delta);
    }
}

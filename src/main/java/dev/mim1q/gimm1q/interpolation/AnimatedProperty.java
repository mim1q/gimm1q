package dev.mim1q.gimm1q.interpolation;

import org.joml.Math;

/**
 * A class for animating a property between two values using an easing function
 *
 * <p>
 * For example, you can create one of these in an {@link net.minecraft.entity.Entity Entity}-inheriting class and
 * transition it whenever you need on the client-side.
 * </p>
 * <pre>{@code
 *     public final AnimatedProperty bodyBounce = new AnimatedProperty(
 *         0F,
 *         Easing::easeOutBounce
 *     );
 * }</pre>
 * <p>
 * And then inside the corresponding {@link net.minecraft.client.render.entity.model.EntityModel EntityModel}'s
 * {@code setAngles} method do something like:
 * </p>
 * <pre>{@code
 *     // Update the bounce value using the provided animation progress
 *     var bounce = entity.bodyBounce.update(animationProgress);
 *     // Use the bounce value to animate the model
 *     this.root.pivotY = bounce;
 * }</pre>
 *
 * @see Easing Easing functions for interpolation
 */
public class AnimatedProperty {
    private float value;
    private float lastValue;
    private float targetValue;
    private float lastTime = 0.0F;
    private float time = 0.0F;
    private float duration = 10.0F;
    private EasingFunction easingFunction;

    /**
     * Creates a new {@link AnimatedProperty} with the given initial value and default easing function
     *
     * @param value          the initial value of the property
     * @param easingFunction the easing function to use for interpolation
     * @see Easing
     */
    public AnimatedProperty(float value, EasingFunction easingFunction) {
        this.value = value;
        this.lastValue = value;
        this.targetValue = value;
        this.easingFunction = easingFunction;
    }

    /**
     * Creates a new {@link AnimatedProperty} with the given initial value. The default easing function is
     * {@link Easing#easeInOutQuad}
     *
     * @param value the initial value of the property
     */
    public AnimatedProperty(float value) {
        this(value, Easing::easeInOutQuad);
    }

    /**
     * Transitions the property to the given target value over the duration, changing the easing function to the
     * specified one.
     *
     * @param targetValue    the target value to transition to
     * @param duration       the duration of the transition, in ticks
     * @param easingFunction the new easing function to use for the transition
     */
    public void transitionTo(float targetValue, float duration, EasingFunction easingFunction) {
        boolean result = this.transitionTo(targetValue, duration);
        if (result) {
            this.easingFunction = easingFunction;
        }
    }

    /**
     * Transitions the property to the given target value over the duration, keeping the current easing function.
     *
     * @param targetValue the target value to transition to
     * @param duration    the duration of the transition, in ticks
     * @return {@code true} if the target value is different from the current target value, {@code false} otherwise
     */
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

    /**
     * Updates the property value based on the provided time
     *
     * @param time the current time in the animation (in ticks)
     * @return the value of the property after updating
     */
    public float update(float time) {
        this.time = time;
        this.value = this.easingFunction.ease(this.lastValue, this.targetValue, this.getProgress());
        return getValue();
    }

    /**
     * Gets the current progress of the transition, from 0.0 to 1.0 (the "{@code delta}" value in the easing function)
     *
     * @return the current progress of the transition
     */
    public float getProgress() {
        return Math.clamp(0.0F, 1.0F, (this.time - this.lastTime) / this.duration);
    }

    /**
     * Gets the current value of the property without updating it
     *
     * @return the current value of the property
     */
    public float getValue() {
        return this.value;
    }

    @FunctionalInterface
    public interface EasingFunction {
        /**
         * Interpolates between two {@code float} values using an easing algorithm
         *
         * @param start the starting value
         * @param end   the ending value
         * @param delta the progress of the transition, from 0.0 to 1.0
         * @return the interpolated value
         */
        float ease(float start, float end, float delta);
    }
}

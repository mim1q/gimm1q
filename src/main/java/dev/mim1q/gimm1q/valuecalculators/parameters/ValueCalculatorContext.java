package dev.mim1q.gimm1q.valuecalculators.parameters;

import dev.mim1q.gimm1q.valuecalculators.ValueCalculator;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Context used to evaluate the Value Calculator, using the provided parameters
 */
public final class ValueCalculatorContext {
    private ValueCalculatorContext() {
    }

    private final Map<ValueCalculatorParameter<?>, Object> parameters = new HashMap<>();

    /**
     * Gets the value of a parameter
     *
     * @param parameter the parameter to get
     * @param <T>       the type of the parameter
     * @return the value of the parameter if it exists, otherwise {@code null}
     */
    @Nullable
    public <T> T get(ValueCalculatorParameter<T> parameter) {
        //noinspection unchecked
        return (T) parameters.get(parameter);
    }

    /**
     * Gets the value of the parameter and transforms it using the mapper, otherwise returns the default value
     *
     * @param parameter    the parameter to get
     * @param mapper       a function to map the value of the parameter to your desired type
     * @param defaultValue the default value
     * @param <R>          the type of the result
     * @param <T>          the type of the parameter
     * @return the mapped value
     */
    public <R, T> R mapOrDefault(ValueCalculatorParameter<T> parameter, Function<T, R> mapper, R defaultValue) {
        T value = get(parameter);
        if (value != null) {
            return mapper.apply(value);
        }
        return defaultValue;
    }

    /**
     * Add a parameter to the context
     * Builtin parameter types are available in {@link ValueCalculatorParameter}
     *
     * @param parameter the parameter to add
     * @param value     the value of the parameter
     * @param <T>       the type of the parameter
     * @return this context
     */
    public <T> ValueCalculatorContext with(ValueCalculatorParameter<T> parameter, T value) {
        parameters.put(parameter, value);
        return this;
    }

    /**
     * Create an empty context
     *
     * @return a new empty {@link ValueCalculator} context
     */
    public static ValueCalculatorContext create() {
        return new ValueCalculatorContext();
    }
}

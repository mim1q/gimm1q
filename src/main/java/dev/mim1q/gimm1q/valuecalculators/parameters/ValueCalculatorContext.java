package dev.mim1q.gimm1q.valuecalculators.parameters;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class ValueCalculatorContext {
    private ValueCalculatorContext() {
    }

    private final Map<ValueCalculatorParameter<?>, Object> parameters = new HashMap<>();

    @Nullable
    public <T> T get(ValueCalculatorParameter<T> parameter) {
        //noinspection unchecked
        return (T) parameters.get(parameter);
    }

    public <R, T> R mapOrDefault(ValueCalculatorParameter<T> parameter, Function<T, R> mapper, R defaultValue) {
        T value = get(parameter);
        if (value != null) {
            return mapper.apply(value);
        }
        return defaultValue;
    }

    public <T> ValueCalculatorContext with(ValueCalculatorParameter<T> parameter, T value) {
        parameters.put(parameter, value);
        return this;
    }

    public static ValueCalculatorContext create() {
        return new ValueCalculatorContext();
    }
}

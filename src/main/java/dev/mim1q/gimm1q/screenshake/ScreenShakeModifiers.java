package dev.mim1q.gimm1q.screenshake;

import java.util.HashMap;
import java.util.Map;

public class ScreenShakeModifiers {
    private static final Map<String, Float> MODIFIERS = new HashMap<>();

    public static float getModifier(String name) {
        return MODIFIERS.getOrDefault(name, 1f);
    }

    public static void setModifier(String name, float value) {
        if (name.isBlank()) throw new IllegalArgumentException("Modifier name cannot be blank");
        MODIFIERS.put(name, value);
    }
}

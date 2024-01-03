package dev.mim1q.gimm1q.interfaces;

public interface ShakeableCameraAccessor {
    float getCameraShake();
    void shakeCamera(float intensity, int duration);
}

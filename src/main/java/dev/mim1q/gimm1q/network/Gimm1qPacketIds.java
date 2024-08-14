package dev.mim1q.gimm1q.network;

import dev.mim1q.gimm1q.Gimm1q;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class Gimm1qPacketIds {
    public static final Identifier CAMERA_SHAKE_S2C = Gimm1q.id("camera_shake");
    public static final Identifier SYNC_VALUE_CALCULATORS_S2C = Gimm1q.id("sync_value_calculators");
}

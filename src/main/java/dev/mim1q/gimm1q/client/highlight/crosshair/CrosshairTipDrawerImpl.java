package dev.mim1q.gimm1q.client.highlight.crosshair;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class CrosshairTipDrawerImpl implements CrosshairTipDrawer {
    public static final CrosshairTipDrawerImpl INSTANCE = new CrosshairTipDrawerImpl();
    private DrawContext drawContext = null;

    private CrosshairTipDrawerImpl() {}

    public static void setDrawContext(DrawContext drawContext) {
        INSTANCE.drawContext = drawContext;
    }

    @Override
    public void drawCrosshairTip(int xOffset, int yOffset, int size, Identifier texture) {
        if (drawContext == null) return;

        var x = MinecraftClient.getInstance().getWindow().getScaledWidth() / 2 + xOffset;
        var y = MinecraftClient.getInstance().getWindow().getScaledHeight() / 2 + yOffset;

        var centerOffset = -size / 2;

        drawContext.drawTexture(
            texture,
            x + centerOffset,
            y + centerOffset,
            0F, 0F,
            size, size,
            size, size
        );
    }
}

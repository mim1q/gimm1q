package dev.mim1q.gimm1q.client.highlight.gui;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class GuiHighlightDrawerImpl implements GuiHighlightDrawer {
    public static final GuiHighlightDrawerImpl INSTANCE = new GuiHighlightDrawerImpl();
    private GuiHighlightDrawerContext context = null;

    private GuiHighlightDrawerImpl() {}

    @Override
    public void highlightItem(int offsetX, int offsetY, int size, int argb, Identifier texture) {
        if (context == null) return;

        var matrices = context.matrixStack();
        matrices.push();
        {
            matrices.translate(0, 0, 300);
            var drawX = context.x() + offsetX;
            var drawY = context.y() + offsetY;

            drawX -= (size / 2) - 8;
            drawY -= (size / 2) - 8;

            var a = ((argb >> 24) & 0xFF) / 255f;
            var r = ((argb >> 16) & 0xFF) / 255f;
            var g = ((argb >> 8) & 0xFF) / 255f;
            var b = (argb & 0xFF) / 255f;

            context.drawingFunction().draw(
                texture,
                drawX, drawX + size,
                drawY, drawY + size,
                0,
                0, 1,
                0, 1,
                r, g, b, a
            );
        }
        matrices.pop();
    }

    public static void setContext(GuiHighlightDrawerContext context) {
        INSTANCE.context = context;
    }

    @FunctionalInterface
    public interface DrawingFunction {
        void draw(Identifier texture, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, float red, float green, float blue, float alpha);
    }

    public record GuiHighlightDrawerContext(
        DrawingFunction drawingFunction,
        MatrixStack matrixStack,
        int x,
        int y
    ) {}
}

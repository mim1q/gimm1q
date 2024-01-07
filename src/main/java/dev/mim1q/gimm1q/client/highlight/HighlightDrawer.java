package dev.mim1q.gimm1q.client.highlight;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public interface HighlightDrawer {
    void drawHighlight(Box box, int colorArgb, int outlineArgb);

    default void drawHighlight(Box box, int colorArgb) {
        drawHighlight(box, colorArgb, 0);
    }

    default void highlightBlock(BlockPos pos, int colorArgb, int outlineArgb) {
        drawHighlight(
            new Box(pos).expand(0.05),
            colorArgb,
            outlineArgb
        );
    }

    default void highlightBlock(BlockPos pos, int colorArgb) {
        highlightBlock(pos, colorArgb, 0);
    }

    default void highlightEntity(Entity entity, int colorArgb, int outlineArgb) {
        var tickDelta = MinecraftClient.getInstance().getTickDelta();
        var pos = entity.getLerpedPos(tickDelta);
        var dxz = (entity.getWidth() / 2) + 0.05;
        var dy = entity.getHeight() + 0.05;
        drawHighlight(
            new Box(pos.x - dxz, pos.y - 0.05, pos.z - dxz, pos.x + dxz, pos.y + dy, pos.z + dxz),
            colorArgb,
            outlineArgb
        );
    }

    default void highlightEntity(Entity entity, int colorArgb) {
        highlightEntity(entity, colorArgb, 0);
    }
}

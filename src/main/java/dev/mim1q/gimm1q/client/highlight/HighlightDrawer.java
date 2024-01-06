package dev.mim1q.gimm1q.client.highlight;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface HighlightDrawer {
    void drawHighlight(Vec3d from, Vec3d to, int color);
    default void highlightBlock(BlockPos pos, int color) {
        drawHighlight(
            Vec3d.of(pos).subtract(0.001, 0.001, 0.001),
            Vec3d.of(pos).add(1.001, 1.001, 1.001),
            color
        );
    }
    default void highlightEntity(Entity entity, int color) {
        var box = entity.getBoundingBox().expand(0.1);
        drawHighlight(
            new Vec3d(box.minX, box.minY, box.minZ),
            new Vec3d(box.maxX, box.maxY, box.maxZ),
            color
        );
    }
}

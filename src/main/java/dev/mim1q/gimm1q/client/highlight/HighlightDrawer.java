package dev.mim1q.gimm1q.client.highlight;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

/**
 * API for drawing highlights around blocks, entities or arbitrary boxes. Should only be used when provided by
 * {@link HighlightDrawerCallback#drawHighlights}
 */
public interface HighlightDrawer {
    /**
     * Draws a highlight around the given Box.
     * If the outline color provided is not transparent, a 1/16 block outline will be drawn around the box.
     * If it is, the full box will be filled with the solid highlight color.
     *
     * @param box         The Box that will be highlighted
     * @param colorArgb   Color of the highlight in ARGB format (0xAARRGGBB)
     * @param outlineArgb Color of the outline in ARGB format (0xAARRGGBB)
     */
    void drawHighlight(Box box, int colorArgb, int outlineArgb);

    /**
     * Draws a highlight without an outline around the given Box.
     *
     * @param box       The Box that will be highlighted
     * @param colorArgb Color of the highlight in ARGB format (0xAARRGGBB)
     * @see #drawHighlight(Box, int, int)
     */
    default void drawHighlight(Box box, int colorArgb) {
        drawHighlight(box, colorArgb, 0);
    }

    /**
     * Draws a highlight around the block at the given Block Position.
     * Doesn't change shape based on the Block's actual Voxel Shape, and will always be a full block size and shape.
     *
     * @param pos         The BlockPos of the block to highlight
     * @param colorArgb   Color of the highlight in ARGB format (0xAARRGGBB)
     * @param outlineArgb Color of the outline in ARGB format (0xAARRGGBB)
     * @see #drawHighlight(Box, int, int)
     */
    default void highlightBlock(BlockPos pos, int colorArgb, int outlineArgb) {
        drawHighlight(
                new Box(pos).expand(0.001),
                colorArgb,
                outlineArgb
        );
    }

    /**
     * Draws a highlight without an outline around the block at the given Block Position.
     *
     * @param pos       The BlockPos of the block to highlight
     * @param colorArgb Color of the highlight in ARGB format (0xAARRGGBB)
     * @see #highlightBlock(BlockPos, int, int)
     */
    default void highlightBlock(BlockPos pos, int colorArgb) {
        highlightBlock(pos, colorArgb, 0);
    }

    /**
     * Draws a highlight around the given Entity.
     * The highlight will be a box that assumes the Entity's Bounding Box size.
     *
     * @param entity      The Entity to highlight
     * @param colorArgb   Color of the highlight in ARGB format (0xAARRGGBB)
     * @param outlineArgb Color of the outline in ARGB format (0xAARRGGBB)
     * @see #drawHighlight(Box, int, int)
     */
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

    /**
     * Draws a highlight without an outline around the given Entity.
     *
     * @param entity    The Entity to highlight
     * @param colorArgb Color of the highlight in ARGB format (0xAARRGGBB)
     * @see #highlightEntity(Entity, int, int)
     */
    default void highlightEntity(Entity entity, int colorArgb) {
        highlightEntity(entity, colorArgb, 0);
    }
}

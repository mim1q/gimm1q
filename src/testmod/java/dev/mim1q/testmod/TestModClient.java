package dev.mim1q.testmod;

import dev.mim1q.gimm1q.client.highlight.HighlightDrawerCallback;
import dev.mim1q.gimm1q.client.highlight.crosshair.CrosshairTipDrawerCallback;
import dev.mim1q.gimm1q.client.highlight.gui.GuiHighlightDrawerCallback;
import dev.mim1q.gimm1q.client.item.handheld.HandheldItemModelRegistry;
import dev.mim1q.testmod.render.EasingTesterRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;

import static dev.mim1q.testmod.TestMod.HIGHLIGHT_STICK;

public class TestModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Entity highlighting
        HighlightDrawerCallback.register((drawer, context) -> {
            var player = context.player();
            if (player.getStackInHand(Hand.MAIN_HAND).isOf(HIGHLIGHT_STICK)) {
                for (var entity : player.clientWorld.getOtherEntities(player, Box.of(player.getPos(), 32, 32, 32))) {
                    drawer.highlightEntity(entity, 0x00000000, 0xFF000000);
                }
            }
        });

        // Item highlighting in GUI
        GuiHighlightDrawerCallback.register((drawer, context) -> {
            var player = context.player();
            if (player.getStackInHand(Hand.MAIN_HAND).isOf(HIGHLIGHT_STICK)
                && context.stack().isOf(Items.DIAMOND)
            ) {
                drawer.highlightItem(0xFFFF0000);
            }
        });

        // Crosshair tip
        CrosshairTipDrawerCallback.register((drawer, context) -> {
            var player = context.player();
            if (player.getStackInHand(Hand.MAIN_HAND).isOf(HIGHLIGHT_STICK)) {
                drawer.drawCrosshairTip(
                    0, 0,
                    8,
                    TestMod.id("textures/gui/crosshair_tip.png")
                );
            }
        });

        // Handheld item model
        HandheldItemModelRegistry.getInstance().register(HIGHLIGHT_STICK);

        // Register the EasingTesterRenderer
        BlockEntityRendererFactories.register(TestMod.EASING_TESTER_BE, EasingTesterRenderer::new);
    }
}

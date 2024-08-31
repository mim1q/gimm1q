package dev.mim1q.gimm1q.client;

import dev.mim1q.gimm1q.client.highlight.HighlightDrawerCallback;
import dev.mim1q.gimm1q.client.highlight.HighlightDrawerCallback.HighlightDrawerContext;
import dev.mim1q.gimm1q.client.highlight.HighlightDrawerImpl;
import dev.mim1q.gimm1q.client.highlight.crosshair.CrosshairTipDrawerCallback;
import dev.mim1q.gimm1q.client.highlight.crosshair.CrosshairTipDrawerCallback.CrosshairTipDrawerContext;
import dev.mim1q.gimm1q.client.highlight.crosshair.CrosshairTipDrawerImpl;
import dev.mim1q.gimm1q.network.Gimm1qClientNetworkHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class Gimm1qClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Gimm1qClientNetworkHandler.init();
        Gimm1qClientCommands.init();

        WorldRenderEvents.AFTER_ENTITIES.register((context) -> {
            var player = MinecraftClient.getInstance().player;
            if (player == null) return;

            HighlightDrawerImpl.setRenderContext(context);
            HighlightDrawerCallback.EVENT.invoker().drawHighlights(
                HighlightDrawerImpl.INSTANCE,
                new HighlightDrawerContext(player)
            );
        });

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            var player = MinecraftClient.getInstance().player;
            if (player == null) return;

            CrosshairTipDrawerImpl.setDrawContext(context);
            CrosshairTipDrawerCallback.EVENT.invoker().drawCrosshairTip(
                CrosshairTipDrawerImpl.INSTANCE,
                new CrosshairTipDrawerContext(player)
            );
        });
    }
}

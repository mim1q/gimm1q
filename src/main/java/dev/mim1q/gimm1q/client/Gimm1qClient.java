package dev.mim1q.gimm1q.client;

import dev.mim1q.gimm1q.network.Gimm1qClientNetworkHandler;
import net.fabricmc.api.ClientModInitializer;

public class Gimm1qClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Gimm1qClientNetworkHandler.init();
    }
}

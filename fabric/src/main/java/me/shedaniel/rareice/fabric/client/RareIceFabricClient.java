package me.shedaniel.rareice.fabric.client;

import me.shedaniel.rareice.*;
import net.fabricmc.api.ClientModInitializer;

public final class RareIceFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        RareIceClient.onInitializeClient();
    }
}

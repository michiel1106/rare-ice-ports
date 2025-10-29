package me.shedaniel.rareice.fabric.client;

import me.shedaniel.rareice.*;
import net.fabricmc.api.ClientModInitializer;

public final class ExampleModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        RareIceClient.onInitializeClient();
    }
}

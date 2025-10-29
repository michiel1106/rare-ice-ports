package me.shedaniel.rareice.neoforge;

import me.shedaniel.rareice.*;
import net.neoforged.api.distmarker.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.*;


@Mod(RareIce.MOD_ID)
public final class ExampleModNeoForge {
    public ExampleModNeoForge() {
        RareIce.onInitialize();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            RareIceClient.onInitializeClient();
        }
    }
}

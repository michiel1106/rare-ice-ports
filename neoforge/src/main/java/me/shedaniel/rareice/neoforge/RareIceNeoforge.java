package me.shedaniel.rareice.neoforge;

import me.shedaniel.rareice.*;
import net.neoforged.api.distmarker.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.*;
import net.neoforged.fml.event.lifecycle.*;
import net.neoforged.fml.loading.*;


@Mod(RareIce.MOD_ID)
public final class RareIceNeoforge {
    public RareIceNeoforge() {
        RareIce.onInitialize();
    }




    @EventBusSubscriber(modid = RareIce.MOD_ID, value = Dist.CLIENT)
    public static class clientEventHandler {
        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            RareIceClient.onInitializeClient();
        }
    }
}

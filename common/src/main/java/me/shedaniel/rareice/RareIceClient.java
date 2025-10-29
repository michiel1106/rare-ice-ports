package me.shedaniel.rareice;

import dev.architectury.injectables.annotations.*;
import dev.architectury.registry.client.rendering.*;
import me.shedaniel.rareice.blocks.entities.RareIceBlockEntityRenderer;


import net.fabricmc.api.*;
import net.minecraft.client.renderer.RenderType;


public class RareIceClient {

    public static void onInitializeClient() {
        RenderTypeRegistry.register(RenderType.translucent(), RareIce.RARE_ICE_BLOCK);
        BlockEntityRendererRegistry.register(RareIce.RARE_ICE_BLOCK_ENTITY_TYPE, RareIceBlockEntityRenderer::new);
    }
}

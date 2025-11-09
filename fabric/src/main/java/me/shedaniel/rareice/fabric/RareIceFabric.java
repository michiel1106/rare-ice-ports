package me.shedaniel.rareice.fabric;

import dev.architectury.registry.level.biome.*;
import me.shedaniel.rareice.*;
import static me.shedaniel.rareice.RareIce.MOD_ID;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.tag.convention.v2.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.world.level.levelgen.*;


public final class RareIceFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        RareIce.onInitialize();
        BiomeModifications.addFeature((biomeSelectionContext -> biomeSelectionContext.hasTag(ConventionalBiomeTags.IS_COLD_OVERWORLD)), GenerationStep.Decoration.UNDERGROUND_ORES, ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(MOD_ID, "rare_ice")));
    }
}

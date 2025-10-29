package me.shedaniel.rareice;

import com.mojang.serialization.*;
import dev.architectury.event.*;
import dev.architectury.event.events.common.*;
import dev.architectury.registry.level.biome.*;
import me.shedaniel.rareice.blocks.RareIceBlock;
import me.shedaniel.rareice.blocks.entities.RareIceBlockEntity;
import me.shedaniel.rareice.world.gen.feature.RareIceConfig;
import me.shedaniel.rareice.world.gen.feature.RareIceCountPlacement;
import me.shedaniel.rareice.world.gen.feature.RareIceFeature;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

public class RareIce {

    public static final String MOD_ID = "rare-ice";
    
    public static final Block RARE_ICE_BLOCK = new RareIceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ICE).isValidSpawn((state, world, pos, type) -> type == EntityType.POLAR_BEAR));
    public static final BlockEntityType<RareIceBlockEntity> RARE_ICE_BLOCK_ENTITY_TYPE = BlockEntityType.Builder.of(RareIceBlockEntity::new, RARE_ICE_BLOCK).build(null);
    public static final Feature<RareIceConfig> RARE_ICE_FEATURE = new RareIceFeature(RareIceConfig.CODEC);

    public static final PlacementModifierType<RareIceCountPlacement> COUNT_PLACEMENT =
            () -> RareIceCountPlacement.CODEC;
    
    public static boolean allowInsertingItemsToIce = true;
    public static int probabilityOfRareIce = 3;
    
    private static void loadConfig(Path file) {
        allowInsertingItemsToIce = true;
        probabilityOfRareIce = 3;
        
        if (Files.exists(file)) {
            try {
                Properties properties = new Properties();
                properties.load(Files.newBufferedReader(file));
                allowInsertingItemsToIce = properties.getProperty("allowInsertingItemsToIce", "true").equals("true");
                probabilityOfRareIce = Integer.parseInt(properties.getProperty("probabilityOfRareIce", "3"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        saveConfig(file);
    }
    
    private static void saveConfig(Path file) {
        try {
            Files.createDirectories(file.getParent());
            Properties properties = new Properties();
            properties.setProperty("allowInsertingItemsToIce", String.valueOf(allowInsertingItemsToIce));
            properties.setProperty("probabilityOfRareIce", String.valueOf(probabilityOfRareIce));
            properties.store(Files.newBufferedWriter(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE), "Rare Ice Configuration");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public static void onInitialize() {
        loadConfig(FabricLoader.getInstance().getConfigDir().resolve("rare-ice.properties"));
        Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, ResourceLocation.fromNamespaceAndPath("rare-ice", "rare_ice_count"), COUNT_PLACEMENT);
        Registry.register(BuiltInRegistries.FEATURE, ResourceLocation.fromNamespaceAndPath("rare-ice", "rare_ice"), RARE_ICE_FEATURE);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("rare-ice", "rare_ice"), RARE_ICE_BLOCK_ENTITY_TYPE);
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath("rare-ice", "rare_ice"), RARE_ICE_BLOCK);
        InteractionEvent.RIGHT_CLICK_BLOCK.register((player, interactionHand, pos, direction) -> {
            if (!allowInsertingItemsToIce) return EventResult.pass();

            Level world = player.level();

            BlockState state = world.getBlockState(pos);
            if (player == null || player.isShiftKeyDown())
                return EventResult.pass();
            if ((state.getBlock() == Blocks.ICE || state.getBlock() == RareIce.RARE_ICE_BLOCK)) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity == null) {
                    world.setBlockAndUpdate(pos, RareIce.RARE_ICE_BLOCK.defaultBlockState());
                    blockEntity = world.getBlockEntity(pos);
                }
                if (blockEntity instanceof RareIceBlockEntity) {
                    RareIceBlockEntity rareIceBlockEntity = (RareIceBlockEntity) blockEntity;
                    ItemStack itemStack = player.getItemInHand(interactionHand);
                    itemStack = player.getAbilities().instabuild ? itemStack.copy() : itemStack;
                    return rareIceBlockEntity.addItem(world, itemStack, player, !world.isClientSide());
                }
            }
            return EventResult.pass();
        });

        BiomeModifications.addProperties((biomeContext, mutable) -> {
            if (biomeContext.getProperties().getClimateProperties().getTemperature() < 0.15F) {
                mutable.getGenerationProperties()
                        .addFeature(
                                GenerationStep.Decoration.UNDERGROUND_ORES,
                                ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath("rare-ice", "rare_ice"))
                        );
            }
        });

    }
}

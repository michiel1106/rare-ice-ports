package me.shedaniel.rareice;

import dev.architectury.event.*;
import dev.architectury.event.events.common.*;
import dev.architectury.platform.*;
import dev.architectury.registry.registries.*;
import me.shedaniel.rareice.blocks.RareIceBlock;
import me.shedaniel.rareice.blocks.entities.RareIceBlockEntity;
import me.shedaniel.rareice.world.gen.feature.RareIceConfig;
import me.shedaniel.rareice.world.gen.feature.RareIceCountPlacement;
import me.shedaniel.rareice.world.gen.feature.RareIceFeature;


import net.minecraft.core.registries.Registries;
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

    public static final String MOD_ID = "rare_ice";



    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(MOD_ID, Registries.BLOCK);
    public static final RegistrySupplier<Block> RARE_ICE_BLOCK = BLOCKS.register("rare_ice",
            () -> new RareIceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ICE)
                    .isValidSpawn((state, world, pos, type) -> type == EntityType.POLAR_BEAR))
    );

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(MOD_ID, Registries.BLOCK_ENTITY_TYPE);
    public static final RegistrySupplier<BlockEntityType<RareIceBlockEntity>> RARE_ICE_BLOCK_ENTITY_TYPE = BLOCK_ENTITIES.register("rare_ice",
            () -> BlockEntityType.Builder.of(RareIceBlockEntity::new, RARE_ICE_BLOCK.get()).build(null)
    );

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(MOD_ID, Registries.FEATURE);
    public static final RegistrySupplier<Feature<RareIceConfig>> RARE_ICE_FEATURE = FEATURES.register("rare_ice",
            () -> new RareIceFeature(RareIceConfig.CODEC)
    );

    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENTS = DeferredRegister.create(MOD_ID, Registries.PLACEMENT_MODIFIER_TYPE);
    public static final RegistrySupplier<PlacementModifierType<RareIceCountPlacement>> COUNT_PLACEMENT = PLACEMENTS.register("rare_ice_count",
            () -> () -> RareIceCountPlacement.CODEC
    );
    
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
        loadConfig(Platform.getConfigFolder().resolve(MOD_ID+".properties"));

        BLOCKS.register();
        BLOCK_ENTITIES.register();
        FEATURES.register();
        PLACEMENTS.register();
        InteractionEvent.RIGHT_CLICK_BLOCK.register((player, interactionHand, pos, direction) -> {
            if (!allowInsertingItemsToIce) return EventResult.pass();

            Level world = player.level();

            BlockState state = world.getBlockState(pos);
            if (player == null || player.isShiftKeyDown())
                return EventResult.pass();
            if ((state.getBlock() == Blocks.ICE || state.getBlock() == RareIce.RARE_ICE_BLOCK)) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity == null) {
                    world.setBlockAndUpdate(pos, RareIce.RARE_ICE_BLOCK.get().defaultBlockState());
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




    }
}

package me.shedaniel.rareice.blocks.entities;

import dev.architectury.event.*;
import me.shedaniel.rareice.ItemLocation;
import me.shedaniel.rareice.RareIce;
import static me.shedaniel.rareice.RareIce.MOD_ID;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RareIceBlockEntity extends BlockEntity implements Clearable {
    private static final RandomSource RANDOM = RandomSource.create();
    private static final ResourceLocation LOOT_TABLE = ResourceLocation.parse(MOD_ID+":chests/rare_ice");
    private final NonNullList<ItemStack> itemsContained;
    private final List<ItemLocation> itemsLocations;
    private boolean setup = false;
    private int delay = 0;
    
    public RareIceBlockEntity(BlockPos pos, BlockState state) {
        super(RareIce.RARE_ICE_BLOCK_ENTITY_TYPE.get(), pos, state);
        this.itemsContained = NonNullList.create();
        this.itemsLocations = new ArrayList<>();
    }
    
    @Override
    public void clearContent() {
        this.itemsContained.clear();
        this.itemsLocations.clear();
    }

    public NonNullList<ItemStack> getItemsContained() {
        return itemsContained;
    }
    
    public List<ItemLocation> getItemsLocations() {
        return itemsLocations;
    }
    
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        this.delay = tag.getInt("RevertDelay");

        this.itemsContained.clear();
        this.itemsLocations.clear();

        ListTag itemsTag = tag.getList("Items", 10);
        for (int i = 0; i < itemsTag.size(); i++) {
            CompoundTag stackTag = itemsTag.getCompound(i);
            ItemStack stack = ItemStack.parse(provider, stackTag).orElse(ItemStack.EMPTY);
            itemsContained.add(stack);
        }

        ListTag locationsTag = tag.getList("ItemLocations", 10);
        for (int i = 0; i < locationsTag.size(); i++) {
            CompoundTag locTag = locationsTag.getCompound(i);
            itemsLocations.add(ItemLocation.fromTag(locTag));
        }
    }



    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("RevertDelay", delay);
        ListTag itemsTag = new ListTag();
        ListTag locationsTag = new ListTag();

        for (int i = 0; i < itemsContained.size(); i++) {
            ItemStack stack = itemsContained.get(i);
            ItemLocation location = itemsLocations.get(i);

            if (!stack.isEmpty()) {
                itemsTag.add(stack.save(provider));
            }
            if (location != null) {
                CompoundTag locTag = new CompoundTag();
                location.toTag(locTag);
                locationsTag.add(locTag);
            }
        }

        tag.put("Items", itemsTag);
        tag.put("ItemLocations", locationsTag);
    }

    public void addLootTable(Level world) {
        setup = true;
    }

    public static void tick(Level world, BlockPos pos, BlockState blockState, RareIceBlockEntity blockEntity) {
        if (blockEntity.setup) {
            blockEntity.setup = false;
            blockEntity.delay = 0;
            ServerLevel server = (ServerLevel) world;
            LootTable lootTable = server.getServer().reloadableRegistries()
                    .getLootTable(ResourceKey.create(Registries.LOOT_TABLE, LOOT_TABLE));
            LootParams.Builder builder = new LootParams.Builder(server);

            List<ItemStack> drops = lootTable.getRandomItems(builder.create(LootContextParamSets.EMPTY));
            int size = Mth.clamp(world.random.nextInt(5) - (world.random.nextInt(1) + 2), 0, drops.size());
            if (!drops.isEmpty()) {
                for (int i = 0; i < size; i++) {
                    int index = world.random.nextInt(drops.size());
                    blockEntity.addItem(world, drops.get(index), null);
                    drops.remove(index);
                }
            }
        } else if (blockEntity.itemsContained.isEmpty()) {
            blockEntity.delay++;
            if (blockEntity.delay > 20) {
                world.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
                blockEntity.setRemoved();
            }
        } else {
            blockEntity.delay = 0;
        }
    }
    
    public EventResult addItem(Level world, ItemStack itemStack, Player nullablePlayer) {
        return addItem(world, itemStack, nullablePlayer, true);
    }

    public EventResult addItem(Level world, ItemStack itemStack, Player nullablePlayer, boolean actuallyDoIt) {
        if (itemStack.getItem() instanceof BlockItem) {
            if (((BlockItem) itemStack.getItem()).getBlock().builtInRegistryHolder().is(BlockTags.ICE))
                return EventResult.pass();
        }

        if (getItemsContained().size() < 8 && itemStack.getCount() >= 1) {
            if (actuallyDoIt) {
                ItemStack added = itemStack.split(1);
                getItemsContained().add(added);
                RandomSource random = world.random != null ? world.random : RANDOM;
                ItemLocation loc = new ItemLocation(random.nextDouble() * .95 + .1, random.nextDouble() * .7 + .1, random.nextDouble() * .95 + .1);
                getItemsLocations().add(loc);
                updateListeners();
            }
            if (nullablePlayer != null && world.isClientSide())
                nullablePlayer.playSound(SoundEvents.CORAL_BLOCK_BREAK, 1.0F, 1.0F);
            return EventResult.interruptTrue();
        }
        return EventResult.interruptFalse();
    }
    
    private void updateListeners() {
        this.setChanged();
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }
}

package me.shedaniel.rareice.blocks;

import com.mojang.serialization.*;
import me.shedaniel.rareice.RareIce;
import me.shedaniel.rareice.blocks.entities.RareIceBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RareIceBlock extends BaseEntityBlock {
    public RareIceBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RareIceBlockEntity(pos, state);
    }
    
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (world.isClientSide) {
            return null;
        } else {
            return createTickerHelper(type, RareIce.RARE_ICE_BLOCK_ENTITY_TYPE.get(), RareIceBlockEntity::tick);
        }
    }
    
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof RareIceBlockEntity) {
                Containers.dropContents(world, pos, ((RareIceBlockEntity) blockEntity).getItemsContained());
            }
            super.onRemove(state, world, pos, newState, moved);
        }
    }
    
    @Environment(EnvType.CLIENT)
    @SuppressWarnings("deprecation")
    @Deprecated
    public boolean skipRendering(BlockState state, BlockState neighbor, Direction facing) {
        return neighbor.getBlock() == this || neighbor.getBlock() == Blocks.ICE || super.skipRendering(state, neighbor, facing);
    }

    @Override
    public void playerDestroy(Level world, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack stack) {
        super.playerDestroy(world, player, pos, state, blockEntity, stack);
        Holder<Enchantment> silkTouchHolder = world.holderLookup(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.SILK_TOUCH);
        int silkTouchLevel = EnchantmentHelper.getItemEnchantmentLevel(silkTouchHolder, stack);
        if (silkTouchLevel == 0) {
            if (world.dimensionType().ultraWarm()) {
                world.removeBlock(pos, false);
            } else {
                BlockState blockState = world.getBlockState(pos.below());
                if (blockState.blocksMotion() || blockState.liquid()) {
                    world.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
                }
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (world.getBrightness(LightLayer.BLOCK, pos) > 11 - state.getLightBlock(world, pos)) {
            this.melt(state, world, pos);
        }
    }
    
    protected void melt(BlockState state, Level world, BlockPos pos) {
        if (world.dimensionType().ultraWarm()) {
            world.removeBlock(pos, false);
        } else {
            world.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
            world.neighborChanged(pos, Blocks.WATER, pos);
        }
    }
    
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return new ItemStack(Blocks.ICE);
    }
}

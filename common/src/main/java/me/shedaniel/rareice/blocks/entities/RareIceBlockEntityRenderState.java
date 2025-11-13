package me.shedaniel.rareice.blocks.entities;

import me.shedaniel.rareice.*;
import net.minecraft.client.renderer.blockentity.state.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.core.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.level.*;

import java.util.*;

public class RareIceBlockEntityRenderState extends BlockEntityRenderState {
    private boolean isRemoved;
    private NonNullList<ItemStack> itemsContained = NonNullList.create();
    private List<ItemLocation> itemsLocations = List.of();
    public List<ItemStackRenderState> itemRenderStates = new ArrayList<>();
    public BlockPos lightPosition;
    public Level blockEntityWorld;

    public void setRemoved(boolean removed) {
        isRemoved = removed;
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    public NonNullList<ItemStack> getItemsContained() {
        return itemsContained;
    }

    public void setItemsContained(NonNullList<ItemStack> itemsContained) {
        this.itemsContained = itemsContained;
    }

    public List<ItemLocation> getItemsLocations() {
        return itemsLocations;
    }

    public void setItemsLocations(List<ItemLocation> itemsLocations) {
        this.itemsLocations = itemsLocations;
    }
}


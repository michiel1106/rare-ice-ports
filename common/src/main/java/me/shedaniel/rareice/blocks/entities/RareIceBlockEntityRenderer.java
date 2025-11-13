package me.shedaniel.rareice.blocks.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.shedaniel.rareice.ItemLocation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.feature.*;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.state.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.*;

import java.util.List;

@Environment(EnvType.CLIENT)
public class RareIceBlockEntityRenderer implements BlockEntityRenderer<RareIceBlockEntity, RareIceBlockEntityRenderState> {
    private final ItemModelResolver itemModelManager;

    public RareIceBlockEntityRenderer(BlockEntityRendererProvider.Context dispatcher) {
        itemModelManager = dispatcher.itemModelResolver();
    }


    @Override
    public boolean shouldRender(RareIceBlockEntity blockEntity, Vec3 vec3) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, vec3);
    }

    @Override
    public RareIceBlockEntityRenderState createRenderState() {
        return new RareIceBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(RareIceBlockEntity blockEntity, RareIceBlockEntityRenderState state, float f, Vec3 vec3, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, f, vec3, crumblingOverlay);
        state.setRemoved(blockEntity.isRemoved());
        state.lightPosition = blockEntity.getBlockPos();
        state.blockEntityWorld = blockEntity.getLevel();
        state.setItemsContained(blockEntity.getItemsContained());
        state.setItemsLocations(blockEntity.getItemsLocations());

        state.itemRenderStates.clear();
        for (ItemStack stack : state.getItemsContained()) {
            ItemStackRenderState itemRenderState = new ItemStackRenderState();
            itemModelManager.updateForTopItem(itemRenderState, stack, ItemDisplayContext.FIXED, blockEntity.getLevel(), null, 0);
            state.itemRenderStates.add(itemRenderState);
        }
    }

    @Override
    public void submit(RareIceBlockEntityRenderState state, PoseStack matrices, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (state.isRemoved()) return;

        NonNullList<ItemStack> contained = state.getItemsContained();
        List<ItemLocation> locations = state.getItemsLocations();
        List<ItemStackRenderState> renderStates = state.itemRenderStates;

        for (int i = 0; i < contained.size(); i++) {
            ItemStack stack = contained.get(i);
            if (stack.isEmpty()) continue;
            ItemLocation location = locations.get(i);
            ItemStackRenderState itemRenderState = renderStates.get(i);

            matrices.pushPose();
            matrices.translate(location.x, location.y, location.z);

            float yawDegrees = (float) (location.yaw * 180.0);
            matrices.mulPose(Axis.YP.rotationDegrees(yawDegrees));
            float pitchDegrees = (float) (location.pitch * 180.0 - 90.0);
            matrices.mulPose(Axis.XP.rotationDegrees(pitchDegrees));
            matrices.scale(0.8f, 0.8f, 0.8f);

            int light = LightTexture.pack(state.blockEntityWorld.getBrightness(LightLayer.BLOCK, state.lightPosition),
                    state.blockEntityWorld.getBrightness(LightLayer.SKY, state.lightPosition));

            itemRenderState.submit(matrices, submitNodeCollector, light, OverlayTexture.NO_OVERLAY, 0);
            matrices.popPose();
        }
    }

}

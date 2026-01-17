package com.startechnology.start_core.client.render;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.client.renderer.machine.IControllerRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.MachineRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.WorkableCasingMachineRenderer;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.data.pack.GTDynamicResourcePack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.startechnology.start_core.StarTCore;
import com.startechnology.start_core.machine.abyssal_containment.StarTAbyssalContainmentMachine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = StarTCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RiftRender extends WorkableCasingMachineRenderer {
    public static final ResourceLocation OVERLAY_MODEL_TEXTURES = GTCEu.id("block/multiblock/fusion_reactor");
    public static final ResourceLocation TEMP = GTCEu.id("block/casings/fusion/advanced_fusion_coil");
    protected static final ResourceLocation RIFT_MODEL = StarTCore.resourceLocation("obj/rift");
    protected static BakedModel RIFT_MODEL_BAKED;

    public RiftRender() {
        super(TEMP, OVERLAY_MODEL_TEXTURES); // temp
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(BlockEntity blockEntity, float partialTicks, PoseStack stack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (!(blockEntity instanceof MetaMachineBlockEntity machineBE)) return;
        if (!(machineBE.metaMachine instanceof StarTAbyssalContainmentMachine machine)) return;
        if (!machine.isFormed() /*|| !machine.isActive()*/) return;
        //var tick = machine.getOffsetTimer() + partialTicks;
        double x = .5, y = .5, z = .5;
        switch (machine.getFrontFacing()) {
            case NORTH -> z = 16.5;
            case SOUTH -> z = -15.5;
            case WEST -> x = 16.5;
            case EAST -> x = -15.5;
        }
        stack.pushPose();
        stack.translate(x, y, z);
        renderRift(stack, buffer);
        stack.popPose();
    }

    private void renderRift(PoseStack stack, MultiBufferSource bufferSource) {
        var scale = .01f * 16.5f;
        stack.pushPose();
        stack.scale(scale, scale, scale);
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(stack.last(), bufferSource.getBuffer(RenderType.solid()), null, RIFT_MODEL_BAKED, 1f, 1f, 1f, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.solid());
        stack.popPose();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasTESR(BlockEntity blockEntity) {
        return true;
    }

    @Override
    public boolean isGlobalRenderer(BlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public void onAdditionalModel(Consumer<ResourceLocation> registry) {
        super.onAdditionalModel(registry);
        registry.accept(RIFT_MODEL);
    }

    @SubscribeEvent
    public static void bakingEvent(ModelEvent.RegisterAdditional event) {
        event.register(RIFT_MODEL);
    }

    @SubscribeEvent
    public static void bakedEvent(ModelEvent.BakingCompleted event) {
        RIFT_MODEL_BAKED = event.getModels().get(RIFT_MODEL);
    }
}

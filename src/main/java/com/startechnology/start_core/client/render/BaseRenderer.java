package com.startechnology.start_core.client.render;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.client.renderer.machine.IControllerRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.WorkableCasingMachineRenderer;
import com.gregtechceu.gtceu.client.util.StaticFaceBakery;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BaseRenderer extends WorkableCasingMachineRenderer implements IControllerRenderer {
    protected final ResourceLocation partModel;

    public BaseRenderer(ResourceLocation baseCasing, ResourceLocation workableModel, ResourceLocation partModel) {
        super(baseCasing, workableModel);
        this.partModel = partModel;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderPartModel(List<BakedQuad> quads, IMultiController controller, IMultiPart part, Direction frontFacing, @Nullable Direction side, RandomSource randomSource, Direction modelFacing, ModelState modelState) {
        if (side != null && modelFacing != null) {
            quads.add(StaticFaceBakery.bakeFace(modelFacing, ModelFactory.getBlockSprite(partModel), modelState));
        }
    }
}

package com.startechnology.start_core.machine;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.startechnology.start_core.StarTCore;
import com.startechnology.start_core.client.render.RiftRender;
import dev.latvian.mods.kubejs.KubeJS;

import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.api.pattern.Predicates.controller;

public class TestMachine {

    public static final MachineDefinition TEST = StarTCore.START_REGISTRATE
            .multiblock("test", WorkableElectricMultiblockMachine::new)
            .langValue("Test")
            .rotationState(RotationState.ALL)
            .recipeType(GTRecipeTypes.CUTTER_RECIPES)
            .appearanceBlock(GTBlocks.CASING_PTFE_INERT)
            .pattern(def -> FactoryBlockPattern.start()
                    .aisle("AAA")
                    .aisle("AAA")
                    .aisle("ACA")
                    .where('A', blocks(GTBlocks.ANTIMATTER_HAZARD_SIGN_BLOCK.get())
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(2))
                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(2))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2))
                            .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where('C', controller(blocks(def.getBlock())))
                    .build()
            ).workableCasingRenderer(KubeJS.id("block/casings/draco_ware_casing"),
                    StarTCore.resourceLocation("block/overlay/abyssal_containment"), false)
            .renderer(RiftRender::new)
            .register();

    public static void init() {}
}

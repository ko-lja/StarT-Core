package com.startechnology.start_core.machine.wireless;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.startechnology.start_core.StarTCore;
import com.startechnology.start_core.integration.ae2.wireless.TowerPredicates;
import com.startechnology.start_core.machine.StarTPartAbility;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

import static com.gregtechceu.gtceu.api.GTValues.LuV;
import static com.gregtechceu.gtceu.api.pattern.Predicates.abilities;
import static com.gregtechceu.gtceu.api.pattern.Predicates.air;
import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.api.pattern.Predicates.controller;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class StarTWirelessMachines {

    public static final MachineDefinition ME_CHANNEL_INPUT_HATCH = REGISTRATE
            .machine("me_channel_input_hatch", MEChannelInputHatchMachine::new)
            .langValue("ME Channel Input Hatch")
            .tier(LuV)
            .rotationState(RotationState.ALL)
            .abilities(StarTPartAbility.ME_CHANNEL_INPUT)
            .colorOverlayTieredHullModel(StarTCore.resourceLocation("block/overlay/appeng/me_channel_input_hatch"))
            .tooltips(
                    Component.translatable("start_core.machine.me_channel_input_hatch.tooltip.0"),
                    Component.translatable("start_core.machine.me_channel_input_hatch.tooltip.1"))
            .register();

    public static final MultiblockMachineDefinition ME_RADIO_TOWER = REGISTRATE
            .multiblock("me_radio_tower", MERadioTowerMachine::new)
            .langValue("ME Radio Tower")
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .recipeType(GTRecipeTypes.DUMMY_RECIPES)
            .tooltips(
                    Component.translatable("start_core.machine.me_radio_tower.tooltip.0"),
                    Component.translatable("start_core.machine.me_radio_tower.tooltip.1"),
                    Component.translatable("start_core.machine.me_radio_tower.tooltip.2"))
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle(
                            "     CCC     ",
                            "     C C     ",
                            "     CCC     ",
                            "      C      ",
                            "  C   C   C  ",
                            "  C   C   C  ",
                            "CCCCCC CCCCCC",
                            "  C   C   C  ",
                            "  C   C   C  ",
                            " CCCCCCCCCCC ",
                            " C H C C H C ",
                            " CCCCCCCCCCC ",
                            "CCCCCCSCCCCCC")
                    .aisle(
                            "    CCCCC    ",
                            "    C   C    ",
                            "    C   C    ",
                            "     CCC     ",
                            "  C   C   C  ",
                            "  CC  C  CC  ",
                            "C CCCC CCCC C",
                            "  CC  C  CC  ",
                            "  C   C   C  ",
                            " C         C ",
                            " C         C ",
                            "C           C",
                            "C           C")
                    .aisle(
                            "   CCCCCCC   ",
                            "   C  C  C   ",
                            "   C  C  C   ",
                            "    CCCCC    ",
                            " C C  C  C C ",
                            " C  C C C  C ",
                            "CC   CCC   CC",
                            " C  C C C  C ",
                            " C C  C  C C ",
                            "C           C",
                            "C H       H C",
                            "C           C",
                            "C           C")
                    .aisle(
                            "  CCCCCCCCC  ",
                            "  C   C   C  ",
                            "  C   C   C  ",
                            "   CCCCCCC   ",
                            "C C   C   C C",
                            "C  C  C  C  C",
                            "C   CCCCC   C",
                            "C  C  C  C  C",
                            "C C   C   C C",
                            "C           C",
                            "C           C",
                            "C           C",
                            "C           C")
                    .aisle(
                            " CCCCCCCCCCC ",
                            " C    C    C ",
                            " C    C    C ",
                            "  CCCCCCCCC  ",
                            "C  C  C  C  C",
                            "C   C C C   C",
                            "C    CCC    C",
                            "C   C C C   C",
                            "C  C  C  C  C",
                            "C           C",
                            "C H       H C",
                            "C           C",
                            "C           C")
                    .aisle(
                            "CCCCCCCCCCCCC",
                            "C     C     C",
                            "C     C     C",
                            " CCCCCCCCCCC ",
                            "C   C C C   C",
                            "C    CCC    C",
                            "C     C     C",
                            "C    CCC    C",
                            "C   C C C   C",
                            "C           C",
                            "C           C",
                            "C           C",
                            "C           C")
                    .aisle(
                            "CCCCCCCCCCCCC",
                            "C     C     C",
                            "C     C     C",
                            "CCCCCCCCCCCCC",
                            "     CCC     ",
                            "     CCC     ",
                            "CCCCCCCCCCCCC",
                            "     CCC     ",
                            "     CCC     ",
                            "CCCCCCCCCCCCC",
                            "CHHCCC CCCHHC",
                            "CEECCC CCCEEC",
                            "CCCCCC CCCCCC")
                    .aisle(
                            "CCCCCCCCCCCCC",
                            "C     C     C",
                            "C     C     C",
                            " CCCCCCCCCCC ",
                            "C   C C C   C",
                            "C    CCC    C",
                            "C     C     C",
                            "C    CCC    C",
                            "C   C C C   C",
                            "C           C",
                            "C           C",
                            "C           C",
                            "C           C")
                    .aisle(
                            " CCCCCCCCCCC ",
                            " C    C    C ",
                            " C    C    C ",
                            "  CCCCCCCCC  ",
                            "C  C  C  C  C",
                            "C   C C C   C",
                            "C    CCC    C",
                            "C   C C C   C",
                            "C  C  C  C  C",
                            "C           C",
                            "C H       H C",
                            "C           C",
                            "C           C")
                    .aisle(
                            "  CCCCCCCCC  ",
                            "  C   C   C  ",
                            "  C   C   C  ",
                            "   CCCCCCC   ",
                            "C C   C   C C",
                            "C  C  C  C  C",
                            "C   CCCCC   C",
                            "C  C  C  C  C",
                            "C C   C   C C",
                            "C           C",
                            "C           C",
                            "C           C",
                            "C           C")
                    .aisle(
                            "   CCCCCCC   ",
                            "   C  C  C   ",
                            "   C  C  C   ",
                            "    CCCCC    ",
                            " C C  C  C C ",
                            " C  C C C  C ",
                            "CC   CCC   CC",
                            " C  C C C  C ",
                            " C C  C  C C ",
                            "C           C",
                            "C H       H C",
                            "C           C",
                            "C           C")
                    .aisle(
                            "    CCCCC    ",
                            "    C   C    ",
                            "    C   C    ",
                            "     CCC     ",
                            "  C   C   C  ",
                            "  CC  C  CC  ",
                            "C CCCC CCCC C",
                            "  CC  C  CC  ",
                            "  C   C   C  ",
                            " C         C ",
                            " C         C ",
                            "C           C",
                            "C           C")
                    .aisle(
                            "     CCC     ",
                            "     C C     ",
                            "     CCC     ",
                            "      C      ",
                            "  C   C   C  ",
                            "  C   C   C  ",
                            "CCCCCC CCCCCC",
                            "  C   C   C  ",
                            "  C   C   C  ",
                            " CCCCCCCCCCC ",
                            " C H C C H C ",
                            " CCCCCCCCCCC ",
                            "CCCCCCCCCCCCC")
                    .where('S', controller(blocks(definition.getBlock())))
                    .where('C', TowerPredicates.tierCasings().setMinGlobalLimited(80))
                    .where('H', TowerPredicates.tierCasings()
                            .or(abilities(StarTPartAbility.ME_CHANNEL_INPUT).setMinGlobalLimited(1).setPreviewCount(16)))
                    .where('E', TowerPredicates.tierCasings()
                            .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(4, 1)))
                    .where(' ', air())
                    .build())
            .shapeInfo(definition -> MultiblockShapeInfo.builder()
                    .aisle(
                            "     CCC     ",
                            "     C C     ",
                            "     CCC     ",
                            "      C      ",
                            "  C   C   C  ",
                            "  C   C   C  ",
                            "CCCCCC CCCCCC",
                            "  C   C   C  ",
                            "  C   C   C  ",
                            " CCCCCCCCCCC ",
                            " C H C C H C ",
                            " CCCCCCCCCCC ",
                            "CCCCCCSCCCCCC")
                    .aisle(
                            "    CCCCC    ",
                            "    C   C    ",
                            "    C   C    ",
                            "     CCC     ",
                            "  C   C   C  ",
                            "  CC  C  CC  ",
                            "C CCCC CCCC C",
                            "  CC  C  CC  ",
                            "  C   C   C  ",
                            " C         C ",
                            " C         C ",
                            "C           C",
                            "C           C")
                    .aisle(
                            "   CCCCCCC   ",
                            "   C  C  C   ",
                            "   C  C  C   ",
                            "    CCCCC    ",
                            " C C  C  C C ",
                            " C  C C C  C ",
                            "CC   CCC   CC",
                            " C  C C C  C ",
                            " C C  C  C C ",
                            "C           C",
                            "C H       H C",
                            "C           C",
                            "C           C")
                    .aisle(
                            "  CCCCCCCCC  ",
                            "  C   C   C  ",
                            "  C   C   C  ",
                            "   CCCCCCC   ",
                            "C C   C   C C",
                            "C  C  C  C  C",
                            "C   CCCCC   C",
                            "C  C  C  C  C",
                            "C C   C   C C",
                            "C           C",
                            "C           C",
                            "C           C",
                            "C           C")
                    .aisle(
                            " CCCCCCCCCCC ",
                            " C    C    C ",
                            " C    C    C ",
                            "  CCCCCCCCC  ",
                            "C  C  C  C  C",
                            "C   C C C   C",
                            "C    CCC    C",
                            "C   C C C   C",
                            "C  C  C  C  C",
                            "C           C",
                            "C H       H C",
                            "C           C",
                            "C           C")
                    .aisle(
                            "CCCCCCCCCCCCC",
                            "C     C     C",
                            "C     C     C",
                            " CCCCCCCCCCC ",
                            "C   C C C   C",
                            "C    CCC    C",
                            "C     C     C",
                            "C    CCC    C",
                            "C   C C C   C",
                            "C           C",
                            "C           C",
                            "C           C",
                            "C           C")
                    .aisle(
                            "CCCCCCCCCCCCC",
                            "C     C     C",
                            "C     C     C",
                            "CCCCCCCCCCCCC",
                            "     CCC     ",
                            "     CCC     ",
                            "CCCCCCCCCCCCC",
                            "     CCC     ",
                            "     CCC     ",
                            "CCCCCCCCCCCCC",
                            "CHHCCC CCCHHC",
                            "CEECCC CCCEEC",
                            "CCCCCC CCCCCC")
                    .aisle(
                            "CCCCCCCCCCCCC",
                            "C     C     C",
                            "C     C     C",
                            " CCCCCCCCCCC ",
                            "C   C C C   C",
                            "C    CCC    C",
                            "C     C     C",
                            "C    CCC    C",
                            "C   C C C   C",
                            "C           C",
                            "C           C",
                            "C           C",
                            "C           C")
                    .aisle(
                            " CCCCCCCCCCC ",
                            " C    C    C ",
                            " C    C    C ",
                            "  CCCCCCCCC  ",
                            "C  C  C  C  C",
                            "C   C C C   C",
                            "C    CCC    C",
                            "C   C C C   C",
                            "C  C  C  C  C",
                            "C           C",
                            "C H       H C",
                            "C           C",
                            "C           C")
                    .aisle(
                            "  CCCCCCCCC  ",
                            "  C   C   C  ",
                            "  C   C   C  ",
                            "   CCCCCCC   ",
                            "C C   C   C C",
                            "C  C  C  C  C",
                            "C   CCCCC   C",
                            "C  C  C  C  C",
                            "C C   C   C C",
                            "C           C",
                            "C           C",
                            "C           C",
                            "C           C")
                    .aisle(
                            "   CCCCCCC   ",
                            "   C  C  C   ",
                            "   C  C  C   ",
                            "    CCCCC    ",
                            " C C  C  C C ",
                            " C  C C C  C ",
                            "CC   CCC   CC",
                            " C  C C C  C ",
                            " C C  C  C C ",
                            "C           C",
                            "C H       H C",
                            "C           C",
                            "C           C")
                    .aisle(
                            "    CCCCC    ",
                            "    C   C    ",
                            "    C   C    ",
                            "     CCC     ",
                            "  C   C   C  ",
                            "  CC  C  CC  ",
                            "C CCCC CCCC C",
                            "  CC  C  CC  ",
                            "  C   C   C  ",
                            " C         C ",
                            " C         C ",
                            "C           C",
                            "C           C")
                    .aisle(
                            "     CCC     ",
                            "     C C     ",
                            "     CCC     ",
                            "      C      ",
                            "  C   C   C  ",
                            "  C   C   C  ",
                            "CCCCCC CCCCCC",
                            "  C   C   C  ",
                            "  C   C   C  ",
                            " CCCCCCCCCCC ",
                            " C H C C H C ",
                            " CCCCCCCCCCC ",
                            "CCCCCCCCCCCCC")
                    .where('S', definition, Direction.NORTH)
                    .where('C', GTBlocks.CASING_STEEL_SOLID.getDefaultState())
                    .where('H', ME_CHANNEL_INPUT_HATCH, Direction.NORTH)
                    .where('E', GTMachines.ENERGY_INPUT_HATCH[LuV], Direction.NORTH)
                    .where(' ', Blocks.AIR)
                    .build())
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    StarTCore.resourceLocation("block/wireless/me_radio_tower"))
            .register();


    public static void init() {}
}

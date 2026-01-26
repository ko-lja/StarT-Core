package com.startechnology.start_core.mixin;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.startechnology.start_core.machine.StarTPartAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = GTMachines.class, remap = false)
public class GTMachinesMixin {
    @Redirect(
            method = "lambda$static$67",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/gregtechceu/gtceu/api/registry/registrate/MachineBuilder;abilities([Lcom/gregtechceu/gtceu/api/machine/multiblock/PartAbility;)Lcom/gregtechceu/gtceu/api/registry/registrate/MachineBuilder;"
            )
    )
    private static MachineBuilder<MachineDefinition> addAbility(MachineBuilder<MachineDefinition> instance, PartAbility[] abilities) {
        return instance.abilities(PartAbility.INPUT_ENERGY, StarTPartAbility.SINGLE_AMP_ENERGY);
    }
}
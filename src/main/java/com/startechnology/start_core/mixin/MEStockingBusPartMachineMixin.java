package com.startechnology.start_core.mixin;

import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.integration.ae2.machine.MEStockingBusPartMachine;
import com.startechnology.start_core.integration.ae2.wireless.IWirelessAEMachine;
import com.startechnology.start_core.integration.ae2.wireless.WirelessMachineTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MEStockingBusPartMachine.class, remap = false)
public abstract class MEStockingBusPartMachineMixin {

    @Inject(method = "attachSideTabs", at = @At("TAIL"))
    private void start_core$attachWirelessTab(TabsWidget sideTabs, CallbackInfo ci) {
        sideTabs.attachSubTab(new WirelessMachineTab((IWirelessAEMachine) this));
    }
}
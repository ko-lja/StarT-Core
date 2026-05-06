package com.startechnology.start_core.mixin;

import com.gregtechceu.gtceu.integration.ae2.machine.MEInputBusPartMachine;
import com.startechnology.start_core.integration.ae2.wireless.IWirelessAEMachine;
import com.startechnology.start_core.integration.ae2.wireless.WirelessTowerDataStickHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MEInputBusPartMachine.class, remap = false)
public abstract class MEInputBusPartMachineMixin {

    @Inject(method = "onDataStickUse", at = @At("HEAD"), cancellable = true)
    private void start_core$pasteWirelessTower(Player player, ItemStack dataStick,
                                               CallbackInfoReturnable<InteractionResult> cir) {
        if (WirelessTowerDataStickHelper.hasTowerData(dataStick)) {
            cir.setReturnValue(WirelessTowerDataStickHelper.pasteTower(player, dataStick, (IWirelessAEMachine) this));
        }
    }
}
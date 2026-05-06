package com.startechnology.start_core.mixin;

import appeng.api.networking.IGridNodeListener;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IDataStickInteractable;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEBusPartMachine;
import com.startechnology.start_core.integration.ae2.wireless.IWirelessAEMachine;
import com.startechnology.start_core.integration.ae2.wireless.WirelessMachineTab;
import com.startechnology.start_core.integration.ae2.wireless.WirelessTowerDataStickHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

@Mixin(value = MEBusPartMachine.class, remap = false)
public abstract class MEBusPartMachineMixin extends ItemBusPartMachine
        implements IWirelessAEMachine, IFancyUIMachine, IDataStickInteractable {

    @Unique
    private static final String start_core$WIRELESS_TOWER_ID = "WirelessTowerId";

    @Unique
    @Nullable
    private UUID start_core$configuredTowerId;

    public MEBusPartMachineMixin(IMachineBlockEntity holder, IO io, Object... args) {
        super(holder, GTValues.LuV, io, args);
    }

    @Override
    @Nullable
    public UUID getConfiguredTowerId() {
        return start_core$configuredTowerId;
    }

    @Override
    public void setConfiguredTowerId(@Nullable UUID id) {
        start_core$configuredTowerId = id;
    }

    @Override
    public List<IFancyUIProvider> getSubTabs() {
        return List.of(new WirelessMachineTab(this));
    }

    @Override
    public InteractionResult onDataStickUse(Player player, ItemStack dataStick) {
        return WirelessTowerDataStickHelper.pasteTower(player, dataStick, this);
    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        if (start_core$configuredTowerId != null) {
            tag.putUUID(start_core$WIRELESS_TOWER_ID, start_core$configuredTowerId);
        }
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        start_core$configuredTowerId = tag.hasUUID(start_core$WIRELESS_TOWER_ID) ?
                tag.getUUID(start_core$WIRELESS_TOWER_ID) : null;
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        super.attachSideTabs(sideTabs);
        sideTabs.attachSubTab(new WirelessMachineTab(this));
    }

    @Inject(method = "onMainNodeStateChanged", at = @At("HEAD"))
    private void start_core$updateWirelessMEStatus(IGridNodeListener.State reason, CallbackInfo ci) {
        updateMEStatus();
    }
}
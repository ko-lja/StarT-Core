package com.startechnology.start_core.integration.jade.provider;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.startechnology.start_core.StarTCore;
import com.startechnology.start_core.integration.ae2.wireless.IWirelessAEMachine;
import com.startechnology.start_core.machine.wireless.MEChannelInputHatchMachine;
import com.startechnology.start_core.machine.wireless.MERadioTowerMachine;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class StarTMEWirelessProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    private static final String RADIO_ACTIVE = "RadioActive";
    private static final String RADIO_CAPACITY = "RadioCapacity";
    private static final String WIRELESS_CONNECTED = "WirelessConnected";
    private static final String CHANNELS_USED = "ChannelsUsed";
    private static final String CHANNEL_CAPACITY = "ChannelCapacity";

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig config) {
        if (blockAccessor.getBlockEntity() instanceof IMachineBlockEntity blockEntity) {
            var machine = blockEntity.getMetaMachine();
            var data = blockAccessor.getServerData().getCompound(getUid().toString());
            if (machine instanceof MERadioTowerMachine) {
                if (data.contains(RADIO_ACTIVE)) {
                    tooltip.add(Component.translatable("start_core.jade.me_radio_tower.connected_machines",
                            data.getInt(RADIO_ACTIVE), data.getInt(RADIO_CAPACITY)).withStyle(ChatFormatting.AQUA));
                }
            } else if (machine instanceof MEChannelInputHatchMachine) {
                if (data.contains(CHANNELS_USED)) {
                    tooltip.add(Component.translatable("start_core.jade.me_channel_input_hatch.channels_used",
                            data.getInt(CHANNELS_USED), data.getInt(CHANNEL_CAPACITY)).withStyle(ChatFormatting.AQUA));
                }
            } else if (data.getBoolean(WIRELESS_CONNECTED)) {
                tooltip.add(Component.translatable("start_core.jade.me_wireless.connected")
                        .withStyle(ChatFormatting.GREEN));
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
        if (!(blockAccessor.getBlockEntity() instanceof IMachineBlockEntity blockEntity)) {
            return;
        }

        var data = compoundTag.getCompound(getUid().toString());
        var machine = blockEntity.getMetaMachine();
        if (machine instanceof MERadioTowerMachine tower) {
            data.putInt(RADIO_ACTIVE, tower.getActiveConnectionCount());
            data.putInt(RADIO_CAPACITY, tower.getUsableConnectionCapacity());
        } else if (machine instanceof MEChannelInputHatchMachine hatch) {
            data.putInt(CHANNELS_USED, hatch.getReservedConnections());
            data.putInt(CHANNEL_CAPACITY, hatch.getChannelCapacity());
        } else if (machine instanceof IWirelessAEMachine wirelessMachine &&
                wirelessMachine.isLinkedToTower() && !wirelessMachine.hasPhysicalGridConnection()) {
            data.putBoolean(WIRELESS_CONNECTED, true);
        }

        if (!data.isEmpty()) {
            compoundTag.put(getUid().toString(), data);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return StarTCore.resourceLocation("me_wireless");
    }
}
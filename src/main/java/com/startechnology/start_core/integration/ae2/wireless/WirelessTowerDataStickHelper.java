package com.startechnology.start_core.integration.ae2.wireless;

import com.startechnology.start_core.machine.wireless.MERadioTowerMachine;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class WirelessTowerDataStickHelper {

    public static final String TAG_WIRELESS_TOWER = "WirelessTower";

    private WirelessTowerDataStickHelper() {}

    public static void writeTower(ItemStack dataStick, MERadioTowerMachine tower) {
        var tag = dataStick.getOrCreateTag();
        var towerTag = new CompoundTag();
        towerTag.putUUID("towerId", tower.getTowerId());
        towerTag.putString("dimension", tower.getLevel().dimension().location().toString());
        towerTag.putInt("x", tower.getPos().getX());
        towerTag.putInt("y", tower.getPos().getY());
        towerTag.putInt("z", tower.getPos().getZ());
        tag.put(TAG_WIRELESS_TOWER, towerTag);
        dataStick.setHoverName(Component.translatable("start_core.machine.me_radio_tower.data_stick.name"));
    }

    public static boolean hasTowerData(ItemStack dataStick) {
        return dataStick.hasTag() && dataStick.getTag() != null && dataStick.getTag().contains(TAG_WIRELESS_TOWER);
    }

    @Nullable
    public static UUID readTowerId(ItemStack dataStick) {
        if (!hasTowerData(dataStick)) {
            return null;
        }
        var towerTag = dataStick.getTag().getCompound(TAG_WIRELESS_TOWER);
        return towerTag.hasUUID("towerId") ? towerTag.getUUID("towerId") : null;
    }

    public static InteractionResult pasteTower(Player player, ItemStack dataStick, IWirelessAEMachine machine) {
        var towerId = readTowerId(dataStick);
        if (towerId == null) {
            return InteractionResult.PASS;
        }
        if (machine.self().getLevel() == null || machine.self().getLevel().isClientSide) {
            return InteractionResult.sidedSuccess(
                    machine.self().getLevel() != null && machine.self().getLevel().isClientSide);
        }

        var previousTowerId = machine.getConfiguredTowerId();
        if (previousTowerId != null && !previousTowerId.equals(towerId)) {
            var previousTower = machine.findTower();
            if (previousTower != null) {
                previousTower.disconnectMachine(machine.getMachineRef(), null);
            }
        }

        machine.setConfiguredTowerId(towerId);
        machine.self().onChanged();
        player.sendSystemMessage(Component.translatable("start_core.machine.me_radio_tower.stored_on_machine",
                shortId(towerId), machine.getDisplayName()));

        var tower = machine.findTower();
        if (tower == null) {
            player.sendSystemMessage(Component.translatable("start_core.machine.me_radio_tower.not_loaded_short"));
        } else {
            tower.requestLink(machine, player);
        }
        return InteractionResult.SUCCESS;
    }

    public static String shortId(UUID uuid) {
        var text = uuid.toString();
        return text.substring(0, Math.min(8, text.length()));
    }
}

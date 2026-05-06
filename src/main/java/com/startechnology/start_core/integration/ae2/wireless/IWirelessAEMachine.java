package com.startechnology.start_core.integration.ae2.wireless;

import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.startechnology.start_core.machine.wireless.MERadioTowerMachine;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * A machine that can connect to ME Radio Towers.
 */
public interface IWirelessAEMachine extends IGridConnectedMachine {

    @Nullable
    UUID getConfiguredTowerId();
    void setConfiguredTowerId(@Nullable UUID id);

    default String getDisplayName() {
        return self().getBlockState().getBlock().getName().getString();
    }

    default WirelessMachineRef getMachineRef() {
        return WirelessMachineRef.of(self().getLevel(), self().getPos());
    }

    default boolean hasPhysicalGridConnection() {
        if (getMainNode().getNode() == null) return false;
        for (var connection : getMainNode().getNode().getConnections()) {
            if (connection.isInWorld()) return true;
        }
        return false;
    }

    @Nullable
    default MERadioTowerMachine findTower() {
        if (!((self().getLevel()) instanceof ServerLevel level)) return null;
        var towerId = getConfiguredTowerId();
        if (towerId == null) return null;
        return WirelessTowerRegistry.findTower(level, towerId);
    }

    default boolean isLinkedToTower() {
        var tower = findTower();
        return tower != null && tower.hasSavedLink(getMachineRef());
    }
}

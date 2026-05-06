package com.startechnology.start_core.integration.ae2.wireless;

import com.startechnology.start_core.machine.wireless.MERadioTowerMachine;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class WirelessTowerRegistry {

    private static final Map<MinecraftServer, Map<UUID, WeakReference<MERadioTowerMachine>>> REGISTRY = new WeakHashMap<>();

    private WirelessTowerRegistry() {}

    public static void register(MERadioTowerMachine tower) {
        if (!(tower.getLevel() instanceof ServerLevel level)) {
            return;
        }
        REGISTRY.computeIfAbsent(level.getServer(), ignored -> new HashMap<>())
                .put(tower.getTowerId(), new WeakReference<>(tower));
    }

    public static void unregister(MERadioTowerMachine tower) {
        if (!(tower.getLevel() instanceof ServerLevel level)) {
            return;
        }
        var towers = REGISTRY.get(level.getServer());
        if (towers == null) {
            return;
        }
        towers.remove(tower.getTowerId());
        if (towers.isEmpty()) {
            REGISTRY.remove(level.getServer());
        }
    }

    @Nullable
    public static MERadioTowerMachine findTower(ServerLevel context, UUID towerId) {
        var towers = REGISTRY.get(context.getServer());
        if (towers == null) {
            return null;
        }
        var reference = towers.get(towerId);
        if (reference == null) {
            return null;
        }
        var tower = reference.get();
        if (tower == null || tower.isInValid()) {
            towers.remove(towerId);
            return null;
        }
        return tower;
    }

    @Nullable
    public static MERadioTowerMachine findNearestTower(ServerLevel context, WirelessMachineRef target) {
        var towers = REGISTRY.get(context.getServer());
        if (towers == null) return null;

        MERadioTowerMachine nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (var entry : towers.entrySet()) {
            var tower = entry.getValue().get();
            if (tower == null || tower.isInValid()) continue;
            if (!(tower.getLevel() instanceof ServerLevel towerLevel)) continue;
            if (!tower.isFormed()) continue;
            if (!tower.getCurrentTier().isWithinRange(towerLevel, tower.getPos(), target.dimension(), target.pos())) continue;
            var distance = towerLevel.dimension().location().equals(target.dimension())
                    ? tower.getPos().distSqr(target.pos())
                    : Double.MAX_VALUE;
            if (nearest == null || distance < nearestDistance) {
                nearest = tower;
                nearestDistance = distance;
            }
        }
        return nearest;
    }
}

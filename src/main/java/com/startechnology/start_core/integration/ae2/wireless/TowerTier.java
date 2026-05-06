package com.startechnology.start_core.integration.ae2.wireless;

import com.gregtechceu.gtceu.common.data.GTBlocks;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

import java.util.Arrays;
import java.util.function.Supplier;

public enum TowerTier {
    STEEL("start_core.machine.me_radio_tower.tier.steel", GTBlocks.CASING_STEEL_SOLID, 64, 8, 32, false),
    STAINLESS("start_core.machine.me_radio_tower.tier.stainless_steel", GTBlocks.CASING_STAINLESS_CLEAN, 256, 32, 128, false),
    TITANIUM("start_core.machine.me_radio_tower.tier.titanium", GTBlocks.CASING_TITANIUM_STABLE, 1024, 64, 512, false),
    TUNGSTENSTEEL("start_core.machine.me_radio_tower.tier.tungstensteel", GTBlocks.CASING_TUNGSTENSTEEL_ROBUST, -1, 128, 2048, true);

    @Getter
    private final String displayNameKey;
    @Getter
    private final Supplier<? extends Block> casing;
    @Getter
    private final int range;
    @Getter
    private final int maxConnections;
    @Getter
    private final long euPerConnection;
    @Getter
    private final boolean crossDimensional;

    TowerTier(String displayNameKey, Supplier<? extends Block> casing, int range, int maxConnections,
              long euPerConnection, boolean crossDimensional) {
        this.displayNameKey = displayNameKey;
        this.casing = casing;
        this.range = range;
        this.maxConnections = maxConnections;
        this.euPerConnection = euPerConnection;
        this.crossDimensional = crossDimensional;
    }

    public boolean isWithinRange(ServerLevel towerLevel, BlockPos towerPos, ResourceLocation targetDimension,
                                 BlockPos targetPos) {
        if (crossDimensional) {
            return true;
        }
        if (!towerLevel.dimension().location().equals(targetDimension)) {
            return false;
        }
        if (range < 0) {
            return true;
        }
        return towerPos.distSqr(targetPos) <= (double) range * range;
    }

    public Component getDisplayName() {
        return Component.translatable(displayNameKey);
    }

    public Component getRangeText() {
        return range < 0 ? Component.translatable("start_core.machine.me_radio_tower.range.infinite") : Component.literal("§d" + range);
    }

    public static TowerTier fromOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= values().length) {
            return STEEL;
        }
        return values()[ordinal];
    }

    public static TowerTier highest() {
        return values()[values().length - 1];
    }

    public static TowerTier forCasing(Block block) {
        return Arrays.stream(values())
                .filter(tier -> tier.getCasing().get() == block)
                .findFirst()
                .orElse(null);
    }
}

package com.startechnology.start_core.integration.ae2.wireless;

import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.error.PatternStringError;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.function.Supplier;

public final class TowerPredicates {

    public static final String MATCH_TIER_KEY = "TowerTier";

    private TowerPredicates() {}

    public static TraceabilityPredicate tierCasings() {
        return Predicates.custom(TowerPredicates::matchTierCasing, () -> Arrays.stream(TowerTier.values())
                .map(TowerTier::getCasing)
                .map(Supplier::get)
                .map(BlockInfo::fromBlock)
                .toArray(BlockInfo[]::new))
                .addTooltips(Component.translatable("start_core.multiblock.pattern.error.me_radio_tower_tier"));

    }

    private static boolean matchTierCasing(MultiblockState state) {
        var block = state.getBlockState().getBlock();
        var matchedTier = TowerTier.forCasing(block);
        if (matchedTier == null) {
            return false;
        }

        var currentTier = state.getMatchContext().get(MATCH_TIER_KEY);
        if (currentTier == null) {
            state.getMatchContext().set(MATCH_TIER_KEY, matchedTier);
            return true;
        }
        if (currentTier == matchedTier) {
            return true;
        }

        state.setError(new PatternStringError("start_core.multiblock.pattern.error.me_radio_tower_tier"));
        return false;
    }
}

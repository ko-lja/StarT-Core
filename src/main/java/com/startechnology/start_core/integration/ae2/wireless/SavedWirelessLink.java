package com.startechnology.start_core.integration.ae2.wireless;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public record SavedWirelessLink(WirelessMachineRef machineRef, @Nullable BlockPos hatchPos) {

    public CompoundTag save() {
        var tag = new CompoundTag();
        tag.put("machine", machineRef.save());
        if (hatchPos != null) {
            tag.putInt("hatchX", hatchPos.getX());
            tag.putInt("hatchY", hatchPos.getY());
            tag.putInt("hatchZ", hatchPos.getZ());
        }
        return tag;
    }

    public static SavedWirelessLink load(CompoundTag tag) {
        BlockPos hatchPos = null;
        if (tag.contains("hatchX")) {
            hatchPos = new BlockPos(tag.getInt("hatchX"), tag.getInt("hatchY"), tag.getInt("hatchZ"));
        }
        return new SavedWirelessLink(WirelessMachineRef.load(tag.getCompound("machine")), hatchPos);
    }

    public SavedWirelessLink withHatch(@Nullable BlockPos newHatchPos) {
        return new SavedWirelessLink(machineRef, newHatchPos == null ? null : newHatchPos.immutable());
    }
}

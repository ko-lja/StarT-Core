package com.startechnology.start_core.integration.ae2.wireless;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public record WirelessMachineRef(ResourceLocation dimension, BlockPos pos) {

    public static WirelessMachineRef of(Level level, BlockPos pos) {
        return new WirelessMachineRef(level.dimension().location(), pos.immutable());
    }

    public CompoundTag save() {
        var tag = new CompoundTag();
        tag.putString("dimension", dimension.toString());
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        return tag;
    }

    public static WirelessMachineRef load(CompoundTag tag) {
        var dimension = ResourceLocation.tryParse(tag.getString("dimension"));
        if (dimension == null) {
            dimension = Level.OVERWORLD.location();
        }
        return new WirelessMachineRef(
                dimension,
                new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")));
    }

    public String dimensionalPos() {
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + " [" + dimension + "]";
    }
}

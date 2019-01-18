package net.gegy1000.psf.api.data;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.material.MapColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

public interface IScannedChunk extends INBTSerializable<NBTTagCompound> {
    @Nonnull
    BlockPos getChunkPos();

    @Nullable
    MapColor getMapColor(int x, int y, int z);
}

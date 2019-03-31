package net.gegy1000.psf.api.data;

import net.minecraft.block.material.MapColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IScannedChunk extends INBTSerializable<NBTTagCompound> {
    @Nonnull
    ChunkPos getChunkPos();

    @Nullable
    MapColor getMapColor(int x, int y, int z);
}

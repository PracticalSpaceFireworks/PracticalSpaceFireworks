package net.gegy1000.psf.server.modules.data;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.RequiredArgsConstructor;
import net.gegy1000.psf.api.data.IScannedChunk;
import net.gegy1000.psf.api.data.ITerrainScan;
import net.minecraft.block.material.MapColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

@RequiredArgsConstructor
public class EmptyTerrainScan implements ITerrainScan {
    private final int scanRange;

    @Nullable
    @Override
    public MapColor getMapColor(int x, int y, int z) {
        return null;
    }

    @Nonnull
    @Override
    public Collection<IScannedChunk> getChunks() {
        List<IScannedChunk> scannedChunks = new ArrayList<>();
        for (int localZ = -scanRange; localZ <= scanRange; localZ++) {
            for (int localX = -scanRange; localX <= scanRange; localX++) {
                scannedChunks.add(new EmptyChunk(new BlockPos(localX, 0, localZ)));
            }
        }
        return scannedChunks;
    }

    @Override
    public int getMinHeight() {
        return 62;
    }

    @Override
    public int getMaxHeight() {
        return 62;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return new NBTTagCompound();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
    }

    @RequiredArgsConstructor
    private class EmptyChunk implements IScannedChunk {
        private final BlockPos pos;

        @Nonnull
        @Override
        public BlockPos getChunkPos() {
            return pos;
        }

        @Nullable
        @Override
        public MapColor getMapColor(int x, int y, int z) {
            return null;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return new NBTTagCompound();
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
        }
    }
}

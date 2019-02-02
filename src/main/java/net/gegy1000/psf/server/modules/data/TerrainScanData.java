package net.gegy1000.psf.server.modules.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.gegy1000.psf.api.data.IScannedChunk;
import net.gegy1000.psf.api.data.ITerrainScan;
import net.minecraft.block.material.MapColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

@RequiredArgsConstructor
public class TerrainScanData implements ITerrainScan {
    private final Map<BlockPos, IScannedChunk> scannedChunks = new HashMap<>();

    @Override
    public void addChunk(IScannedChunk chunkData) {
        this.scannedChunks.put(chunkData.getChunkPos(), chunkData);
    }
    
    @Override
    public void removeChunk(IScannedChunk chunk) {
        scannedChunks.remove(chunk.getChunkPos());
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        NBTTagList chunkList = new NBTTagList();
        for (IScannedChunk scannedChunk : this.scannedChunks.values()) {
            chunkList.appendTag(scannedChunk.serializeNBT());
        }
        compound.setTag("chunks", chunkList);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        NBTTagList chunkList = compound.getTagList("chunks", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < chunkList.tagCount(); i++) {
            ChunkData chunk = new ChunkData();
            chunk.deserializeNBT(chunkList.getCompoundTagAt(i));
            this.scannedChunks.put(chunk.chunkPos, chunk);
        }
    }

    @Nullable
    @Override
    public MapColor getMapColor(int x, int y, int z) {
        BlockPos chunkPos = new BlockPos(x >> 4, y >> 4, z >> 4);
        IScannedChunk scannedChunk = this.scannedChunks.get(chunkPos);
        if (scannedChunk != null) {
            return scannedChunk.getMapColor(x & 15, y & 15, z & 15);
        }
        return null;
    }

    @Nonnull
    @Override
    public Collection<IScannedChunk> getChunks() {
        return this.scannedChunks.values();
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChunkData implements IScannedChunk {
        private BlockPos chunkPos;
        private byte[] blockColors;

        @Nonnull
        @Override
        public BlockPos getChunkPos() {
            return this.chunkPos;
        }

        @Override
        @Nullable
        public MapColor getMapColor(int x, int y, int z) {
            int idx = this.blockColors[x << 8 | z << 4 | y] & 0xFF;
            if (idx == 0) {
                return null;
            }
            return MapColor.COLORS[idx];
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setLong("pos", this.chunkPos.toLong());
            compound.setByteArray("block_data", this.blockColors);
            return compound;
        }

        @Override
        public void deserializeNBT(NBTTagCompound compound) {
            this.chunkPos = BlockPos.fromLong(compound.getLong("pos"));
            this.blockColors = compound.getByteArray("block_data");
        }
    }

    @Override
    public int getMinHeight() {
        return 0;
    }

    @Override
    public int getMaxHeight() {
        return scannedChunks.keySet().stream().mapToInt(BlockPos::getY).map(i -> (i << 4) + 8).max().orElse(0);
    }
}

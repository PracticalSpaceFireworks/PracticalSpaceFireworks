package net.gegy1000.psf.server.modules.data;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.gegy1000.psf.api.data.IScannedChunk;
import net.gegy1000.psf.api.data.ITerrainScan;
import net.minecraft.block.material.MapColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.util.Constants;

@RequiredArgsConstructor
public class TerrainScanData implements ITerrainScan {
    private final Map<ChunkPos, IScannedChunk> scannedChunks = new HashMap<>();

    @Getter
    @Setter
    private int minHeight;

    @Getter
    @Setter
    private int maxHeight;

    public void addChunk(ChunkData chunkData) {
        this.scannedChunks.put(chunkData.chunkPos, chunkData);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        NBTTagList chunkList = new NBTTagList();
        for (IScannedChunk scannedChunk : this.scannedChunks.values()) {
            chunkList.appendTag(scannedChunk.serializeNBT());
        }
        compound.setTag("chunks", chunkList);
        compound.setShort("min_height", (short) this.minHeight);
        compound.setShort("max_height", (short) this.maxHeight);
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
        this.minHeight = compound.getShort("min_height");
        this.maxHeight = compound.getShort("max_height");
    }

    @Nullable
    @Override
    public MapColor getMapColor(int x, int y, int z) {
        ChunkPos chunkPos = new ChunkPos(x >> 4, z >> 4);
        IScannedChunk scannedChunk = this.scannedChunks.get(chunkPos);
        if (scannedChunk != null) {
            return scannedChunk.getMapColor(x & 15, y & 255, z & 15);
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
        private ChunkPos chunkPos;
        private byte[] blockColors;

        @Nonnull
        @Override
        public ChunkPos getChunkPos() {
            return this.chunkPos;
        }

        @Override
        @Nullable
        public MapColor getMapColor(int x, int y, int z) {
            int idx = this.blockColors[x << 12 | z << 8 | y] & 0xFF;
            if (idx == 0) {
                return null;
            }
            return MapColor.COLORS[idx];
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger("chunk_x", this.chunkPos.x);
            compound.setInteger("chunk_z", this.chunkPos.z);
            compound.setByteArray("block_data", this.blockColors);
            return compound;
        }

        @Override
        public void deserializeNBT(NBTTagCompound compound) {
            this.chunkPos = new ChunkPos(compound.getInteger("chunk_x"), compound.getInteger("chunk_z"));
            this.blockColors = compound.getByteArray("block_data");
        }
    }
}

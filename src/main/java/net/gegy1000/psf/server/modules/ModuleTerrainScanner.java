package net.gegy1000.psf.server.modules;

import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.capability.CapabilityModuleData;
import net.gegy1000.psf.server.modules.data.TerrainScanData;
import net.minecraft.block.material.MapColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModuleTerrainScanner extends EmptyModule {
    private static final int SCAN_RANGE = 2;

    private TerrainScanData scanData;
    private boolean scanned;

    public ModuleTerrainScanner() {
        super("terrain_scanner");
    }

    @Override
    public void onSatelliteTick(ISatellite satellite) {
        World world = satellite.getWorld();
        BlockPos position = satellite.getPosition();
        if (world.isBlockLoaded(position)) {
            this.scanData = this.scan(world, new ChunkPos(position.getX() >> 4, position.getZ() >> 4));
        }
    }

    private TerrainScanData scan(World world, ChunkPos origin) {
        TerrainScanData scanData = new TerrainScanData();
        for (int chunkZ = -SCAN_RANGE; chunkZ <= SCAN_RANGE; chunkZ++) {
            for (int chunkX = -SCAN_RANGE; chunkX <= SCAN_RANGE; chunkX++) {
                Chunk chunk = world.getChunkFromChunkCoords(origin.x + chunkX, origin.z + chunkZ);
                scanData.addChunk(this.scanChunk(new ChunkPos(chunkX, chunkZ), chunk));
            }
        }

        this.scanned = true;
        return scanData;
    }

    private TerrainScanData.ChunkData scanChunk(ChunkPos chunkPos, Chunk chunk) {
        byte[] blockColors = new byte[65536];

        int index = 0;
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                for (int localY = 0; localY < 256; localY++) {
                    MapColor mapColor = chunk.getBlockState(localX, localY, localZ).getMapColor(null, null);
                    blockColors[index++] = (byte) mapColor.colorIndex;
                }
            }
        }

        return new TerrainScanData.ChunkData(chunkPos, blockColors);
    }

    @Override
    public int getTickInterval() {
        return this.scanned ? 1200 : 1;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        if (this.scanData != null) {
            compound.setTag("scan_data", this.scanData.serializeNBT());
        }
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        if (compound.hasKey("scan_data")) {
            this.scanData = new TerrainScanData();
            this.scanData.deserializeNBT(compound.getCompoundTag("scan_data"));
            this.scanned = true;
        }
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityModuleData.TERRAIN_SCAN && this.scanData != null) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityModuleData.TERRAIN_SCAN && this.scanData != null) {
            return CapabilityModuleData.TERRAIN_SCAN.cast(this.scanData);
        }
        return super.getCapability(capability, facing);
    }
}
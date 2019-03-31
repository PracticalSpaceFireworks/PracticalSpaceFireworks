package net.gegy1000.psf.server.modules;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.api.module.IEnergyStats;
import net.gegy1000.psf.api.module.ITerrainScan;
import net.gegy1000.psf.api.module.ModuleCapabilities;
import net.gegy1000.psf.server.modules.cap.EnergyStats;
import net.gegy1000.psf.server.modules.data.EmptyTerrainScan;
import net.gegy1000.psf.server.modules.data.TerrainScanData;
import net.minecraft.block.material.MapColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;

public class ModuleTerrainScanner extends EmptyModule {
    public static final int SCAN_RANGE = 2;

    private static final int SCAN_INTERVAL = 1200;
    private static final int POWER_PER_TICK = 288000;

    private static final IEnergyStats ENERGY_STATS = new EnergyStats(POWER_PER_TICK, 0, SCAN_INTERVAL);

    private TerrainScanData scanData;
    private boolean scanned;

    public ModuleTerrainScanner() {
        super("terrain_scanner");
    }

    @Override
    public void onSatelliteTick(@Nonnull ISatellite satellite) {
        World world = satellite.getWorld();
        BlockPos position = satellite.getPosition();
        if (world.isBlockLoaded(position) || this.scanData == null && satellite.tryExtractEnergy(POWER_PER_TICK)) {
            this.scanData = this.scan(world, new ChunkPos(position.getX() >> 4, position.getZ() >> 4));
            this.dirty(true);
        }
    }

    private TerrainScanData scan(World world, ChunkPos origin) {
        int minHeight = 256;
        int maxHeight = 0;

        TerrainScanData scanData = new TerrainScanData();
        for (int chunkZ = -SCAN_RANGE; chunkZ <= SCAN_RANGE; chunkZ++) {
            for (int chunkX = -SCAN_RANGE; chunkX <= SCAN_RANGE; chunkX++) {
                Chunk chunk = world.getChunk(origin.x + chunkX, origin.z + chunkZ);
                scanData.addChunk(this.scanChunk(new ChunkPos(chunkX, chunkZ), chunk));

                int height = chunk.getTopFilledSegment() + 8;
                if (height > maxHeight) {
                    maxHeight = height;
                }
                if (height < minHeight) {
                    minHeight = height;
                }
            }
        }

        scanData.setMinHeight(minHeight);
        scanData.setMaxHeight(maxHeight);

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
        return this.scanned ? SCAN_INTERVAL : 1;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = super.serializeNBT();
        if (this.scanData != null) {
            compound.setTag("scan_data", this.scanData.serializeNBT());
        }
        return compound;
    }

    @Override
    public void deserializeNBT(@Nonnull NBTTagCompound compound) {
        super.deserializeNBT(compound);
        if (compound.hasKey("scan_data")) {
            this.scanData = new TerrainScanData();
            this.scanData.deserializeNBT(compound.getCompoundTag("scan_data"));
            this.scanned = true;
        }
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == ModuleCapabilities.TERRAIN_SCAN || capability == ModuleCapabilities.ENERGY_STATS) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == ModuleCapabilities.TERRAIN_SCAN) {
            ITerrainScan terrainScan = this.scanData != null ? this.scanData : new EmptyTerrainScan(SCAN_RANGE);
            return ModuleCapabilities.TERRAIN_SCAN.cast(terrainScan);
        } else if (capability == ModuleCapabilities.ENERGY_STATS) {
            return ModuleCapabilities.ENERGY_STATS.cast(ENERGY_STATS);
        }
        return super.getCapability(capability, facing);
    }
}

package net.gegy1000.psf.server.modules;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.api.module.IEnergyStats;
import net.gegy1000.psf.api.module.IWeatherData;
import net.gegy1000.psf.api.module.ModuleCapabilities;
import net.gegy1000.psf.server.modules.cap.EnergyStats;
import net.gegy1000.psf.server.modules.data.WeatherScanData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.capabilities.Capability;

public class ModuleWeatherScanner extends EmptyModule {
    private static final int SCAN_INTERVAL = 1200;
    private static final int POWER_PER_TICK = 288000;

    private static final IEnergyStats ENERGY_STATS = new EnergyStats(POWER_PER_TICK, 0, SCAN_INTERVAL);

    private WeatherScanData scanData;
    private boolean scanned;

    public ModuleWeatherScanner() {
        super("weather_scanner");
    }

    @Override
    public void onSatelliteTick(@Nonnull ISatellite satellite) {
        World world = satellite.getWorld();
        BlockPos position = satellite.getPosition();
        if (world.isBlockLoaded(position) || this.scanData == null && satellite.tryExtractEnergy(POWER_PER_TICK)) {
            this.scanData = this.scan(world);
            this.dirty(true);
        }
    }

    private WeatherScanData scan(World world) {
        WorldInfo worldInfo = world.getWorldInfo();

        WeatherScanData data = new WeatherScanData();
        data.setCleanWeatherTime(worldInfo.getCleanWeatherTime());
        data.setRainTime(worldInfo.getRainTime());
        data.setThunderTime(worldInfo.getThunderTime());

        this.scanned = true;
        return data;
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
            this.scanData = new WeatherScanData();
            this.scanData.deserializeNBT(compound.getCompoundTag("scan_data"));
            this.scanned = true;
        }
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == ModuleCapabilities.WEATHER_DATA || capability == ModuleCapabilities.ENERGY_STATS) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == ModuleCapabilities.WEATHER_DATA) {
            IWeatherData weatherData = this.scanData != null ? this.scanData : new WeatherScanData();
            return ModuleCapabilities.WEATHER_DATA.cast(weatherData);
        } else if (capability == ModuleCapabilities.ENERGY_STATS) {
            return ModuleCapabilities.ENERGY_STATS.cast(ENERGY_STATS);
        }
        return super.getCapability(capability, facing);
    }
}

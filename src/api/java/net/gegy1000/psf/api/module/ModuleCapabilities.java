package net.gegy1000.psf.api.module;

import javax.annotation.Nonnull;

import net.gegy1000.psf.api.IAdditionalMass;
import net.gegy1000.psf.api.ILaser;
import net.gegy1000.psf.server.modules.cap.EnergyStats;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

@SuppressWarnings("null")
public class ModuleCapabilities {
    
    @CapabilityInject(IAdditionalMass.class)
    @Nonnull
    public static final Capability<IAdditionalMass> ADDITIONAL_MASS = null;

    @CapabilityInject(IEntityList.class)
    @Nonnull
    public static final Capability<IEntityList> ENTITY_LIST = null;

    @CapabilityInject(ITerrainScan.class)
    @Nonnull
    public static final Capability<ITerrainScan> TERRAIN_SCAN = null;
    
    @CapabilityInject(ILaser.class)
    @Nonnull
    public static final Capability<ILaser> SPACE_LASER = null;

    @CapabilityInject(EnergyStats.class)
    @Nonnull
    public static final Capability<IEnergyStats> ENERGY_STATS = null;

    @CapabilityInject(IWeatherData.class)
    @Nonnull
    public static final Capability<IWeatherData> WEATHER_DATA = null;

}

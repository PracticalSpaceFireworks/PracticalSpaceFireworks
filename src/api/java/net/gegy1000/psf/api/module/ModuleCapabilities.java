package net.gegy1000.psf.api.module;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

import javax.annotation.Nonnull;

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

    @CapabilityInject(IEnergyStats.class)
    @Nonnull
    public static final Capability<IEnergyStats> ENERGY_STATS = null;

    @CapabilityInject(IWeatherData.class)
    @Nonnull
    public static final Capability<IWeatherData> WEATHER_DATA = null;

    @CapabilityInject(IThruster.class)
    @Nonnull
    public static final Capability<IThruster> THRUSTER = null;

    @CapabilityInject(ISeparator.class)
    @Nonnull
    public static final Capability<ISeparator> SEPARATOR = null;

}

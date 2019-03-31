package net.gegy1000.psf.server.modules;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Collection;

import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.api.module.IEnergyStats;
import net.gegy1000.psf.api.module.ModuleCapabilities;
import net.gegy1000.psf.server.modules.cap.EnergyStats;
import net.gegy1000.psf.server.util.LogisticGrowthCurve;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class ModuleSolarPanel extends EmptyModule {
    
    private static final LogisticGrowthCurve DN_CURVE = new LogisticGrowthCurve(-0.9, 0, -80);
    private static final LogisticGrowthCurve UP_CURVE = new LogisticGrowthCurve(0.9, 0, -80);
    
    /**
     * Slow piecewise calculation, based on https://www.desmos.com/calculator/z8zyaidfgh
     * <p>
     * ONLY call with 0 <= x <= 1
     */
    private static double calcMultiplier(double angle) {
        if (angle <= 0.5) {
            return 1 + DN_CURVE.get(angle - 0.25);
        } else {
            return 0.1 + UP_CURVE.get(angle - 0.75);
        }
    }
    
    private static long lastTick = -1;
    private static double multCache;
    public static double getMultiplier(World world) {
        if (world.getWorldTime() != lastTick) {
            lastTick = world.getWorldTime();
            multCache = calcMultiplier(world.getCelestialAngle(0));
        }
        return multCache;
    }
     
    private final IEnergyStats usageStats;
    
    public ModuleSolarPanel(String subtype, int perTick) {
        super("solar_panel_" + subtype);
        usageStats = new EnergyStats(perTick, 0);
    }

    @Override
    public void onSatelliteTick(@Nonnull ISatellite satellite) {
        super.onSatelliteTick(satellite);

        Collection<IEnergyStorage> powerSources = satellite.getModuleCaps(CapabilityEnergy.ENERGY);

        int powerToProvide = MathHelper.ceil(usageStats.getMaxDrain() * getMultiplier(satellite.getWorld()));
        for (IEnergyStorage source : powerSources) {
            powerToProvide -= source.receiveEnergy(powerToProvide, false);
            if (powerToProvide <= 0) {
                break;
            }
        }
    }

    @Override
    public int getTickInterval() {
        return 1;
    }
    
    @Override
    public boolean groupWith(IModule other) {
        return super.groupWith(other) && other.getName().equals(getName());
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return super.hasCapability(capability, facing) || capability == ModuleCapabilities.ENERGY_STATS;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == ModuleCapabilities.ENERGY_STATS) {
            return ModuleCapabilities.ENERGY_STATS.cast(usageStats);
        }
        return super.getCapability(capability, facing);
    }
}

package net.gegy1000.psf.server.modules;

import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.capability.CapabilityModuleData;
import net.gegy1000.psf.server.modules.cap.EnergyStats;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class ModuleSolarPanel extends EmptyModule {
     
    private final EnergyStats USAGE_STATS;
    
    public ModuleSolarPanel(int perTick) {
        super("solar_panel");
        USAGE_STATS = new EnergyStats(0, perTick);
    }

    @Override
    public void onSatelliteTick(@Nonnull ISatellite satellite) {
        super.onSatelliteTick(satellite);

        Collection<IEnergyStorage> powerSources = satellite.getModuleCaps(CapabilityEnergy.ENERGY);

        if (satellite.getWorld().isDaytime()) {
            int powerToProvide = USAGE_STATS.getMaxDrain();
            powerToProvide = -(int)((powerToProvide / (36000000.0)) * satellite.getWorld().getWorldTime() * (satellite.getWorld().getWorldTime() - 12000)) + 1;
            for (IEnergyStorage source : powerSources) {
                powerToProvide -= source.receiveEnergy(powerToProvide, false);
                if (powerToProvide <= 0) {
                    break;
                }
            }
        }
    }

    @Override
    public int getTickInterval() {
        return 1;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return super.hasCapability(capability, facing) || capability == CapabilityModuleData.ENERGY_STATS;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityModuleData.ENERGY_STATS) {
            return CapabilityModuleData.ENERGY_STATS.cast(USAGE_STATS);
        }
        return super.getCapability(capability, facing);
    }
}

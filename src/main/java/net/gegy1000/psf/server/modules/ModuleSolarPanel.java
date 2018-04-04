package net.gegy1000.psf.server.modules;

import net.gegy1000.psf.api.IEnergyHandler;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.capability.CapabilityModuleData;
import net.gegy1000.psf.server.modules.cap.EnergyHandler;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class ModuleSolarPanel extends EmptyModule {

    private static final int POWER_PER_TICK = 120;

    private static final IEnergyHandler ENERGY_HANDLER = new EnergyHandler(0, POWER_PER_TICK);

    public ModuleSolarPanel() {
        super("solar_panel");
    }

    @Override
    public void onSatelliteTick(ISatellite satellite) {
        super.onSatelliteTick(satellite);

        Collection<IEnergyStorage> powerSources = satellite.getModuleCaps(CapabilityEnergy.ENERGY);

        if (satellite.getWorld().isDaytime()) {
            int powerToProvide = POWER_PER_TICK;
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
        return super.hasCapability(capability, facing) || capability == CapabilityModuleData.ENERGY_HANDLER;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityModuleData.ENERGY_HANDLER) {
            return CapabilityModuleData.ENERGY_HANDLER.cast(ENERGY_HANDLER);
        }
        return super.getCapability(capability, facing);
    }
}

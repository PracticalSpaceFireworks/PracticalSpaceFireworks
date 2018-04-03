package net.gegy1000.psf.server.modules;

import net.gegy1000.psf.api.ISatellite;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.Collection;

public class ModuleSolarPanel extends EmptyModule {

    private static final int POWER_PER_TICK = 120;

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
}

package net.gegy1000.psf.server.entity.spacecraft;

import com.google.common.collect.ImmutableList;

import javax.vecmath.Point3d;

import java.util.List;

import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.ISpacecraftMetadata;
import net.gegy1000.psf.api.module.ModuleCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerConcatenate;

public class SpacecraftMetadata implements ISpacecraftMetadata {
    private final ImmutableList<IModule> modules;
    private final ImmutableList<IFluidHandler> fuelTanks;
    private final ImmutableList<Thruster> thrusters;
    private final double mass;
    private final Point3d com;

    private final double totalForce;
    private final int totalDrain;

    public SpacecraftMetadata(List<IModule> modules, List<IFluidHandler> fuelTanks, ImmutableList<Thruster> thrusters, double mass, Point3d com) {
        this.modules = ImmutableList.copyOf(modules);
        this.fuelTanks = ImmutableList.copyOf(fuelTanks);
        this.thrusters = thrusters;
        this.mass = mass;
        this.com = com;

        double totalForce = 0.0;
        for (Thruster thruster : this.thrusters) {
            totalForce += thruster.getForce();
        }
        this.totalForce = totalForce;

        int totalDrain = 0;
        for (Thruster thruster : this.thrusters) {
            totalDrain += thruster.getDrain();
        }
        this.totalDrain = totalDrain;
    }

    @Override
    public List<Thruster> getThrusters() {
        return this.thrusters;
    }

    @Override
    public double getTotalForce() {
        return this.totalForce;
    }

    @Override
    public int getTotalFuelDrain() {
        return this.totalDrain;
    }

    @Override
    public double getMass() {
        double mass = this.mass;
        for (IModule module : this.modules) {
            if (module.hasCapability(ModuleCapabilities.ADDITIONAL_MASS, null)) {
                double additionalMass = module.getCapability(ModuleCapabilities.ADDITIONAL_MASS, null).getAdditionalMass();
                mass += additionalMass;
            }
        }
        return mass;
    }

    @Override
    public Point3d getCoM() {
        return com;
    }

    @Override
    public IFluidHandler buildFuelHandler() {
        return new FluidHandlerConcatenate(this.fuelTanks);
    }
}

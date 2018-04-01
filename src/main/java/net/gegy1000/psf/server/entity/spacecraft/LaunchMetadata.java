package net.gegy1000.psf.server.entity.spacecraft;

import com.google.common.collect.ImmutableList;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.server.capability.CapabilityModuleData;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerConcatenate;

import java.util.List;

public class LaunchMetadata {
    private final ImmutableList<IModule> modules;
    private final ImmutableList<IFluidHandler> fuelTanks;
    private final ImmutableList<Thruster> thrusters;
    private final double mass;

    private final double totalForce;
    private final int totalDrain;

    public LaunchMetadata(List<IModule> modules, List<IFluidHandler> fuelTanks, ImmutableList<Thruster> thrusters, double mass) {
        this.modules = ImmutableList.copyOf(modules);
        this.fuelTanks = ImmutableList.copyOf(fuelTanks);
        this.thrusters = thrusters;
        this.mass = mass;

        double totalForce = 0.0;
        for (Thruster thruster : this.thrusters) {
            totalForce += thruster.force;
        }
        this.totalForce = totalForce;

        int totalDrain = 0;
        for (Thruster thruster : this.thrusters) {
            totalDrain += thruster.drain;
        }
        this.totalDrain = totalDrain;
    }

    public List<Thruster> getThrusters() {
        return this.thrusters;
    }

    public double getTotalForce() {
        return this.totalForce;
    }

    public int getTotalFuelDrain() {
        return this.totalDrain;
    }

    public double getMass() {
        double mass = this.mass;
        for (IModule module : this.modules) {
            if (module.hasCapability(CapabilityModuleData.ADDITIONAL_MASS, null)) {
                double additionalMass = module.getCapability(CapabilityModuleData.ADDITIONAL_MASS, null).getAdditionalMass();
                mass += additionalMass;
            }
        }
        return mass;
    }

    public IFluidHandler buildFuelHandler() {
        return new FluidHandlerConcatenate(this.fuelTanks);
    }

    public static class Thruster {
        private final BlockPos pos;
        private final double force;
        private final int drain;

        public Thruster(BlockPos pos, double force, int drain) {
            this.pos = pos;
            this.force = force;
            this.drain = drain;
        }

        public BlockPos getPos() {
            return this.pos;
        }

        public double getForce() {
            return this.force;
        }

        public int getDrain() {
            return this.drain;
        }
    }
}

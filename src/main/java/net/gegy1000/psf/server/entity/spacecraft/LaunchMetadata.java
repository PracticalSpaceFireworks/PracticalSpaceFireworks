package net.gegy1000.psf.server.entity.spacecraft;

import com.google.common.collect.ImmutableList;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.server.capability.CapabilityModuleData;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class LaunchMetadata {
    private final ImmutableList<IModule> modules;
    private final ImmutableList<Thruster> thrusters;
    private final double mass;

    public LaunchMetadata(List<IModule> modules, ImmutableList<Thruster> thrusters, double mass) {
        this.modules = ImmutableList.copyOf(modules);
        this.thrusters = thrusters;
        this.mass = mass;
    }

    public List<Thruster> getThrusters() {
        return this.thrusters;
    }

    public double getTotalForce() {
        double force = 0.0;
        for (Thruster thruster : this.thrusters) {
            force += thruster.force;
        }
        return force;
    }

    public double getTotalAcceleration() {
        return this.getTotalForce() / this.getMass();
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

    public static class Thruster {
        private final BlockPos pos;
        private final double force;

        public Thruster(BlockPos pos, double force) {
            this.pos = pos;
            this.force = force;
        }

        public BlockPos getPos() {
            return this.pos;
        }

        public double getForce() {
            return this.force;
        }
    }
}

package net.gegy1000.psf.api;

import javax.vecmath.Point3d;

import java.util.List;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.capability.IFluidHandler;

public interface ISpacecraftMetadata {

    List<Thruster> getThrusters();

    double getTotalForce();

    int getTotalFuelDrain();

    double getMass();

    Point3d getCoM();

    IFluidHandler buildFuelHandler();
    
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

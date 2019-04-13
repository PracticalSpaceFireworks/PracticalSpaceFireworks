package net.gegy1000.psf.api.spacecraft;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.Collection;

public interface IStageMetadata {

    Collection<Thruster> getThrusters();

    Collection<Separator> getSeparators();

    double getTotalForce();

    int getTotalFuelDrain();

    IFluidHandler getFuelHandler();

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

    public static class Separator {
        private final BlockPos pos;
        private final EnumFacing direction;

        public Separator(BlockPos pos, EnumFacing direction) {
            this.pos = pos;
            this.direction = direction;
        }

        public BlockPos getPos() {
            return pos;
        }

        public EnumFacing getDirection() {
            return direction;
        }

        public BlockPos getConnectedPos() {
            return pos.offset(direction);
        }
    }
}

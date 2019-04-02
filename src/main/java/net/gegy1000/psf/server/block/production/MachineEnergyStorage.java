package net.gegy1000.psf.server.block.production;

import net.gegy1000.psf.server.block.Machine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.EnergyStorage;

// TODO: Name subject to change with `Machine`
public final class MachineEnergyStorage extends EnergyStorage {
    private final TileEntity entity;

    private ActivityState active = ActivityState.UNKNOWN;

    public MachineEnergyStorage(TileEntity entity, int capacity) {
        super(capacity);
        this.entity = entity;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int amount = super.extractEnergy(maxExtract, simulate);
        setActive(amount >= maxExtract ? ActivityState.ACTIVE : ActivityState.INACTIVE);

        return amount;
    }

    private void setActive(ActivityState state) {
        if (this.active != state) {
            this.active = state;
            updateBlockState(state == ActivityState.ACTIVE);
        }
    }

    private void updateBlockState(boolean active) {
        World world = entity.getWorld();
        BlockPos pos = entity.getPos();
        if (world.isRemote) return;

        IBlockState state = world.getBlockState(pos);
        world.setBlockState(pos, state.withProperty(Machine.ACTIVE, active));
    }

    public boolean isActive() {
        if (active == ActivityState.UNKNOWN) {
            active = getStateFromWorld();
        }
        return active == ActivityState.ACTIVE;
    }

    private ActivityState getStateFromWorld() {
        World world = entity.getWorld();
        if (world.isRemote) return ActivityState.UNKNOWN;

        if (Machine.isActive(world.getBlockState(entity.getPos()))) {
            return ActivityState.ACTIVE;
        } else {
            return ActivityState.INACTIVE;
        }
    }

    enum ActivityState {
        UNKNOWN,
        ACTIVE,
        INACTIVE
    }
}

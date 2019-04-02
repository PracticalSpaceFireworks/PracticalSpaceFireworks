package net.gegy1000.psf.server.block.production;

import net.gegy1000.psf.server.block.Machine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// TODO: Name subject to change with `Machine`
public final class MachineStateTracker {
    private final TileEntity entity;

    private ActivityState state = ActivityState.UNKNOWN;

    public MachineStateTracker(TileEntity entity) {
        this.entity = entity;
    }

    public void resetActivity() {
        setState(ActivityState.INACTIVE);
    }

    public void markActive() {
        setState(ActivityState.ACTIVE);
    }

    private void setState(ActivityState state) {
        if (this.state != state) {
            this.state = state;
            updateBlockState(state == ActivityState.ACTIVE);
        }
    }

    private void updateBlockState(boolean active) {
        World world = entity.getWorld();
        BlockPos pos = entity.getPos();
        if (world.isRemote) return;

        IBlockState state = world.getBlockState(pos);
        world.setBlockState(pos, state.withProperty(Machine.ACTIVE, active), 3);

        entity.validate();
        world.setTileEntity(pos, entity);
    }

    public boolean isActive() {
        if (state == ActivityState.UNKNOWN) {
            state = getStateFromWorld();
        }
        return state == ActivityState.ACTIVE;
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

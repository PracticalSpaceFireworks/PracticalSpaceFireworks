package net.gegy1000.psf.server.block.production;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.capability.MultiTankFluidHandler;
import net.gegy1000.psf.server.capability.TypedFluidTank;
import net.gegy1000.psf.server.init.PSFFluids;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@ParametersAreNonnullByDefault
public class TileAirSeparator extends TileEntity implements ITickable {
    private static final double OXYGEN_AMOUNT = 0.21;
    private static final double NITROGEN_AMOUNT = 0.78;

    private static final int TANK_SIZE = 1000;
    private static final int DISTILL_PER_TICK = 2;

    private final IFluidHandler localInput = new TypedFluidTank(TANK_SIZE, PSFFluids.compressedAir());
    private final IFluidHandler localOxygen = new TypedFluidTank(MathHelper.floor(TANK_SIZE * OXYGEN_AMOUNT), PSFFluids.liquidOxygen());
    private final IFluidHandler localNitrogen = new TypedFluidTank(MathHelper.floor(TANK_SIZE * NITROGEN_AMOUNT), PSFFluids.liquidNitrogen());

    private MasterInfo masterInfo = null;
    private final List<TileAirSeparator> connected = new ArrayList<>();

    private boolean connectedDirty = true;

    @Override
    public void update() {
        if (connectedDirty || connected.stream().anyMatch(TileEntity::isInvalid)) {
            scanConnected();
        }

        if (!world.isRemote && masterInfo != null) {
            masterInfo.update();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setTag("input_fluid", CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.writeNBT(localInput, null));
        compound.setTag("output_oxygen", CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.writeNBT(localOxygen, null));
        compound.setTag("output_nitrogen", CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.writeNBT(localNitrogen, null));
        if (masterInfo != null && masterInfo.isMaster(this)) {
            compound.setTag("master", masterInfo.serialize(new NBTTagCompound()));
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.readNBT(localInput, null, compound.getTag("input_fluid"));
        CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.readNBT(localOxygen, null, compound.getTag("output_oxygen"));
        CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.readNBT(localNitrogen, null, compound.getTag("output_nitrogen"));
        if (compound.hasKey("master")) {
            masterInfo = MasterInfo.deserialize(compound.getCompoundTag("master"));
        }
        connectedDirty = true;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.readFromNBT(tag);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return masterInfo != null && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (masterInfo != null && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(getFluidHandler(masterInfo, facing));
        }
        return super.getCapability(capability, facing);
    }

    private IFluidHandler getFluidHandler(MasterInfo masterInfo, @Nullable EnumFacing facing) {
        if (facing == null) {
            return masterInfo.combinedStorage;
        }

        if (facing.getAxis() == EnumFacing.Axis.Y) {
            if (masterInfo.state == State.DRAINING) {
                if (facing == EnumFacing.UP) {
                    return masterInfo.combinedNitrogen;
                } else if (facing == EnumFacing.DOWN) {
                    return masterInfo.combinedOxygen;
                }
            }
        } else if (masterInfo.state == State.FILLING) {
            return masterInfo.combinedInput;
        }

        return EmptyFluidHandler.INSTANCE;
    }

    public List<TileAirSeparator> getConnectedSeparators() {
        if (connected.isEmpty() || connectedDirty) {
            scanConnected();
        }
        return Collections.unmodifiableList(connected);
    }

    private void scanConnected() {
        connected.clear();
        connectedDirty = false;

        BlockPos pos = getPos();

        TileEntity entity;
        while ((entity = world.getTileEntity(pos)) instanceof TileAirSeparator) {
            connected.add((TileAirSeparator) entity);
            pos = pos.offset(EnumFacing.UP);
        }

        pos = getPos().down();
        while ((entity = world.getTileEntity(pos)) instanceof TileAirSeparator) {
            connected.add((TileAirSeparator) entity);
            pos = pos.offset(EnumFacing.DOWN);
        }

        if (connected.isEmpty()) {
            PracticalSpaceFireworks.LOGGER.warn("Found no air separator connections! This should never happen!");
            return;
        }

        // Choose the separator with the lowest Y to be the master as that is consistent across all calls
        TileAirSeparator master = connected.stream().min(Comparator.comparingInt(te -> te.pos.getY())).get();

        if (getPos().equals(master.getPos())) {
            if (masterInfo == null) {
                masterInfo = new MasterInfo(getPos());
            }
            masterInfo.transferTo(this);
            masterInfo.buildTotalStorage(connected);
        } else {
            if (master.connectedDirty) {
                master.scanConnected();
            }
            masterInfo = master.masterInfo;
        }
    }

    public void markConnectedDirty() {
        connectedDirty = true;
    }

    private enum State {
        FILLING {
            @Override
            protected State update(MasterInfo master, IFluidTankProperties properties, @Nullable FluidStack contents) {
                if (contents != null && contents.amount >= properties.getCapacity()) {
                    return State.DISTILLING;
                }
                return this;
            }
        },
        DISTILLING {
            @Override
            protected State update(MasterInfo master, IFluidTankProperties properties, @Nullable FluidStack contents) {
                if (contents == null || contents.amount <= 0) {
                    master.combinedOxygen.fill(new FluidStack(PSFFluids.liquidOxygen(), (int) Math.round(master.oxygenRemainder)), true);
                    master.combinedNitrogen.fill(new FluidStack(PSFFluids.liquidNitrogen(), (int) Math.round(master.nitrogenRemainder)), true);
                    master.oxygenRemainder = 0.0;
                    master.nitrogenRemainder = 0.0;
                    return State.DRAINING;
                }

                FluidStack drainedInput = master.combinedInput.drain(DISTILL_PER_TICK, true);
                if (drainedInput != null && drainedInput.amount > 0) {
                    double oxygenAmount = drainedInput.amount * OXYGEN_AMOUNT + master.oxygenRemainder;
                    double nitrogenAmount = drainedInput.amount * NITROGEN_AMOUNT + master.nitrogenRemainder;
                    int oxygenFillAmount = MathHelper.floor(oxygenAmount);
                    int nitrogenFillAmount = MathHelper.floor(nitrogenAmount);
                    master.oxygenRemainder = oxygenAmount - oxygenFillAmount;
                    master.nitrogenRemainder = nitrogenAmount - nitrogenFillAmount;

                    // TODO: What if these outputs are full?
                    master.combinedOxygen.fill(new FluidStack(PSFFluids.liquidOxygen(), oxygenFillAmount), true);
                    master.combinedNitrogen.fill(new FluidStack(PSFFluids.liquidNitrogen(), nitrogenFillAmount), true);
                }

                return this;
            }
        },
        DRAINING {
            @Override
            protected State update(MasterInfo master, IFluidTankProperties properties, @Nullable FluidStack contents) {
                FluidStack nitrogenContents = master.combinedNitrogen.getTankProperties()[0].getContents();
                FluidStack oxygenContents = master.combinedOxygen.getTankProperties()[0].getContents();
                if ((nitrogenContents == null || nitrogenContents.amount <= 0) && (oxygenContents == null || oxygenContents.amount <= 0)) {
                    return State.FILLING;
                }

                return this;
            }
        };

        protected abstract State update(MasterInfo master, IFluidTankProperties properties, @Nullable FluidStack contents);
    }

    private static class MasterInfo {
        private BlockPos masterPos;

        private IFluidHandler combinedInput = EmptyFluidHandler.INSTANCE;
        private IFluidHandler combinedNitrogen = EmptyFluidHandler.INSTANCE;
        private IFluidHandler combinedOxygen = EmptyFluidHandler.INSTANCE;
        private IFluidHandler combinedStorage = EmptyFluidHandler.INSTANCE;

        private State state = State.DRAINING;

        private double oxygenRemainder;
        private double nitrogenRemainder;

        private MasterInfo(BlockPos masterPos) {
            this.masterPos = masterPos;
        }

        void transferTo(TileAirSeparator tile) {
            this.masterPos = tile.pos;
        }

        void update() {
            IFluidTankProperties inputProperties = combinedInput.getTankProperties()[0];
            FluidStack inputContents = inputProperties.getContents();
            // TODO: Would it be possible to end up with more than the maximum amount of fluid (with all combined)?
            state = state.update(this, inputProperties, inputContents);
        }

        void buildTotalStorage(List<TileAirSeparator> connected) {
            List<IFluidHandler> inputHandlers = new ArrayList<>();
            List<IFluidHandler> nitrogenHandlers = new ArrayList<>();
            List<IFluidHandler> oxygenHandlers = new ArrayList<>();
            for (TileAirSeparator separator : connected) {
                inputHandlers.add(separator.localInput);
                nitrogenHandlers.add(separator.localNitrogen);
                oxygenHandlers.add(separator.localOxygen);
            }

            List<IFluidHandler> allHandlers = new ArrayList<>(inputHandlers.size() + nitrogenHandlers.size() + oxygenHandlers.size());
            allHandlers.addAll(inputHandlers);
            allHandlers.addAll(nitrogenHandlers);
            allHandlers.addAll(oxygenHandlers);

            combinedStorage = new MultiTankFluidHandler(allHandlers);
            combinedInput = new MultiTankFluidHandler(inputHandlers);
            combinedNitrogen = new MultiTankFluidHandler(nitrogenHandlers);
            combinedOxygen = new MultiTankFluidHandler(oxygenHandlers);
        }

        boolean isMaster(TileAirSeparator separator) {
            return separator.pos.equals(this.masterPos);
        }

        NBTTagCompound serialize(NBTTagCompound compound) {
            compound.setInteger("x", masterPos.getX());
            compound.setInteger("y", masterPos.getY());
            compound.setInteger("z", masterPos.getZ());
            compound.setDouble("oxygen_remainder", oxygenRemainder);
            compound.setDouble("nitrogen_remainder", nitrogenRemainder);
            compound.setByte("state", (byte) state.ordinal());
            return compound;
        }

        static MasterInfo deserialize(NBTTagCompound compound) {
            BlockPos masterPos = new BlockPos(compound.getInteger("x"), compound.getInteger("y"), compound.getInteger("z"));
            MasterInfo info = new MasterInfo(masterPos);
            info.oxygenRemainder = compound.getDouble("oxygen_remainder");
            info.nitrogenRemainder = compound.getDouble("nitrogen_remainder");
            info.state = State.values()[compound.getByte("state") % State.values().length];
            return info;
        }
    }
}

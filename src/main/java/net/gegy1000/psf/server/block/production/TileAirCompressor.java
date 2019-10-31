package net.gegy1000.psf.server.block.production;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import java.util.EnumMap;

import lombok.RequiredArgsConstructor;
import net.gegy1000.psf.server.block.production.state.StateMachine;
import net.gegy1000.psf.server.block.production.state.StateMachineBuilder;
import net.gegy1000.psf.server.block.production.state.StateType;
import net.gegy1000.psf.server.capability.TypedFluidTank;
import net.gegy1000.psf.server.init.PSFFluids;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerConcatenate;

@ParametersAreNonnullByDefault
public class TileAirCompressor extends TileEntity implements ITickable {

    static final int TANK_SIZE = 1000;
    private static final int COMPRESS_PER_TICK = 2;

    private static final int DRAIN_PER_TICK = 60;

    private static final int ENERGY_BUFFER = 500;
    private static final int ENERGY_PER_TICK = 20;

    public static final StateType FILLING_STATE = new StateType("filling");
    public static final StateType COMPRESSING_STATE = new StateType("compressing");
    public static final StateType DRAINING_STATE = new StateType("draining");

    private static final StateMachineBuilder<StepCtx> STATE_MACHINE_BUILDER = new StateMachineBuilder<StepCtx>()
            .withInitState(FILLING_STATE)
            .withStateChangeInterval(20)
            .withStep(FILLING_STATE, ctx -> {
                if (ctx.tankContents != null && ctx.tankContents.amount >= ctx.tankProperties.getCapacity()) {
                    return COMPRESSING_STATE;
                }
                return FILLING_STATE;
            })
            .withStep(COMPRESSING_STATE, ctx -> {
                if (ctx.tankContents == null || ctx.tankContents.amount == 0) {
                    return DRAINING_STATE;
                }
                if (ctx.tile.energyStorage.extractEnergy(ENERGY_PER_TICK, false) >= ENERGY_PER_TICK) {
                    FluidStack drained = ctx.tile.inputStorage.drainInternal(new FluidStack(PSFFluids.FILTERED_AIR.getFluid(), COMPRESS_PER_TICK), true);
                    if (drained != null && drained.amount > 0) {
                        ctx.tile.outputStorage.fillInternal(new FluidStack(PSFFluids.COMPRESSED_AIR.getFluid(), drained.amount), true);
                        ctx.markActive();
                    }
                    return COMPRESSING_STATE;
                }
                return DRAINING_STATE;
            })
            .withStep(DRAINING_STATE, ctx -> {
                ctx.markActive();

                EnumFacing facing = ctx.tile.getOutputSide();
                TileEntity outputEntity = ctx.tile.outputs.get(facing);

                if (outputEntity == null || outputEntity.isInvalid()) {
                    outputEntity = ctx.tile.world.getTileEntity(ctx.tile.pos.offset(facing));
                    if (outputEntity == null) {
                        ctx.tile.outputs.remove(facing);
                    } else {
                        ctx.tile.outputs.put(facing, outputEntity);
                    }
                }

                if (outputEntity != null) {
                    IFluidHandler output = outputEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite());
                    if (output != null) {
                        FluidUtil.tryFluidTransfer(output, ctx.tile.outputStorage, DRAIN_PER_TICK, true);
                    }
                }

                FluidStack outputContents = ctx.tile.outputStorage.getTankProperties()[0].getContents();
                if (outputContents == null || outputContents.amount <= 0) {
                    return FILLING_STATE;
                }

                return DRAINING_STATE;
            });

    private final FluidTank inputStorage = new TypedFluidTank(TANK_SIZE, PSFFluids.FILTERED_AIR.getFluid(), TypedFluidTank.IO.IN);
    private final FluidTank outputStorage = new TypedFluidTank(TANK_SIZE, PSFFluids.COMPRESSED_AIR.getFluid(), TypedFluidTank.IO.OUT);
    private final IFluidHandler combinedStorage = new FluidHandlerConcatenate(inputStorage, outputStorage);

    private final IEnergyStorage energyStorage = new EnergyStorage(ENERGY_BUFFER);
    private final MachineStateTracker stateTracker = new MachineStateTracker(this);

    private final EnumMap<EnumFacing, TileEntity> outputs = new EnumMap<>(EnumFacing.class);

    private final StateMachine<StepCtx> stateMachine = STATE_MACHINE_BUILDER.build();

    @Override
    public void update() {
        if (!world.isRemote) {
            IFluidTankProperties inputProperties = inputStorage.getTankProperties()[0];
            StepCtx ctx = new StepCtx(this, inputProperties, inputProperties.getContents());
            stateMachine.update(ctx, world.getTotalWorldTime());
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setTag("input_fluid", CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.writeNBT(inputStorage, null));
        compound.setTag("output_fluid", CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.writeNBT(outputStorage, null));
        compound.setTag("energy", CapabilityEnergy.ENERGY.writeNBT(energyStorage, null));
        compound.setTag("state", stateMachine.serialize(new NBTTagCompound()));
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        Capability<IFluidHandler> fluidCap = CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
        if (compound.hasKey("input_fluid")) {
            fluidCap.readNBT(inputStorage, null, compound.getTag("input_fluid"));
        }
        if (compound.hasKey("output_fluid")) {
            fluidCap.readNBT(outputStorage, null, compound.getTag("output_fluid"));
        }
        if (compound.hasKey("energy")) {
            CapabilityEnergy.ENERGY.readNBT(energyStorage, null, compound.getTag("energy"));
        }
        stateMachine.deserialize(compound);
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.readFromNBT(tag);
    }

    public StateType getState() {
        return stateMachine.getState();
    }
    
    IFluidHandler getAirTank() {
        return inputStorage;
    }
    
    IFluidHandler getCompressedAirTank() {
        return outputStorage;
    }

    @Nonnull
    private EnumFacing getInputSide() {
        return world.getBlockState(pos).getValue(BlockHorizontal.FACING).rotateY();
    }

    @Nonnull
    private EnumFacing getOutputSide() {
        return getInputSide().getOpposite();
    }

    private boolean canDoEnergy(@Nullable EnumFacing facing) {
        return canDoFluid(facing) || facing == EnumFacing.DOWN;
    }

    private boolean canDoFluid(@Nullable EnumFacing facing) {
        return facing == null || facing == getOutputSide() || facing == getInputSide();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return (capability == CapabilityEnergy.ENERGY && canDoEnergy(facing))
                || (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && canDoFluid(facing));
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY && canDoEnergy(facing)) {
            return CapabilityEnergy.ENERGY.cast(energyStorage);
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && canDoFluid(facing)) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(getFluidHandler(facing));
        }
        return super.getCapability(capability, facing);
    }

    private IFluidHandler getFluidHandler(@Nullable EnumFacing facing) {
        if (facing == null) {
            return combinedStorage;
        } else if (!canDoFluid(facing) || stateMachine.getState() == COMPRESSING_STATE) {
            return EmptyFluidHandler.INSTANCE;
        }
        return facing == getOutputSide() ? outputStorage : inputStorage;
    }

    @RequiredArgsConstructor
    static class StepCtx {
        final TileAirCompressor tile;
        final IFluidTankProperties tankProperties;
        @Nullable
        final FluidStack tankContents;

        void markActive() {
            tile.stateTracker.markActive();
        }
    }
}

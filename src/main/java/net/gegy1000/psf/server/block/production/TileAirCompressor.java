package net.gegy1000.psf.server.block.production;

import net.gegy1000.psf.server.capability.TypedFluidTank;
import net.gegy1000.psf.server.fluid.PSFFluidRegistry;
import net.gegy1000.psf.server.util.FluidTransferUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerConcatenate;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumMap;

@ParametersAreNonnullByDefault
public class TileAirCompressor extends TileEntity implements ITickable {
    private static final EnumFacing[] OUTPUT_SIDES = new EnumFacing[] { EnumFacing.UP, EnumFacing.DOWN };

    private static final int TANK_SIZE = 1000;
    private static final int COMPRESS_PER_TICK = 2;

    private static final int DRAIN_PER_TICK = 60;

    private static final int ENERGY_BUFFER = 500;
    private static final int ENERGY_PER_TICK = 20;

    private static final int STATE_CHANGE_TIME = 20;

    private final IFluidHandler inputStorage = new TypedFluidTank(TANK_SIZE, PSFFluidRegistry.FILTERED_AIR);
    private final IFluidHandler outputStorage = new TypedFluidTank(TANK_SIZE, PSFFluidRegistry.COMPRESSED_AIR);
    private final IFluidHandler combinedStorage = new FluidHandlerConcatenate(inputStorage, outputStorage);

    private final IEnergyStorage energyStorage = new EnergyStorage(ENERGY_BUFFER);

    private final EnumMap<EnumFacing, TileEntity> outputs = new EnumMap<>(EnumFacing.class);

    private State state = State.FILLING;
    private long nextStateTickTime;

    @Override
    public void update() {
        if (!world.isRemote && world.getTotalWorldTime() > nextStateTickTime) {
            IFluidTankProperties inputProperties = inputStorage.getTankProperties()[0];
            FluidStack inputContents = inputProperties.getContents();
            State last = state;
            state = state.update(this, inputProperties, inputContents);
            if (last != state) {
                nextStateTickTime = world.getTotalWorldTime() + STATE_CHANGE_TIME;
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setTag("input_fluid", CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.writeNBT(inputStorage, null));
        compound.setTag("output_fluid", CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.writeNBT(outputStorage, null));
        compound.setTag("energy", CapabilityEnergy.ENERGY.writeNBT(energyStorage, null));
        compound.setByte("state", (byte) state.ordinal());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.readNBT(inputStorage, null, compound.getTag("input_fluid"));
        CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.readNBT(outputStorage, null, compound.getTag("output_fluid"));
        CapabilityEnergy.ENERGY.readNBT(energyStorage, null, compound.getTag("energy"));
        state = State.values()[compound.getByte("state") % State.values().length];
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.readFromNBT(tag);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityEnergy.ENERGY || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(energyStorage);
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(getFluidHandler(facing));
        }
        return super.getCapability(capability, facing);
    }

    private IFluidHandler getFluidHandler(@Nullable EnumFacing facing) {
        if (facing != null && state != State.FILLING) {
            return EmptyFluidHandler.INSTANCE;
        } else if (facing == null) {
            return combinedStorage;
        }
        return inputStorage;
    }

    private enum State {
        FILLING {
            @Override
            protected State update(TileAirCompressor compressor, IFluidTankProperties properties, @Nullable FluidStack contents) {
                if (contents != null && contents.amount >= properties.getCapacity()) {
                    return State.COMPRESSING;
                }
                return this;
            }
        },
        COMPRESSING {
            @Override
            protected State update(TileAirCompressor compressor, IFluidTankProperties properties, @Nullable FluidStack contents) {
                if (contents == null || contents.amount == 0) {
                    return State.DRAINING;
                }
                if (compressor.energyStorage.extractEnergy(ENERGY_PER_TICK, false) >= ENERGY_PER_TICK) {
                    FluidStack drained = compressor.inputStorage.drain(new FluidStack(PSFFluidRegistry.FILTERED_AIR, COMPRESS_PER_TICK), true);
                    if (drained != null && drained.amount > 0) {
                        compressor.outputStorage.fill(new FluidStack(PSFFluidRegistry.COMPRESSED_AIR, drained.amount), true);
                    }
                }
                return this;
            }
        },
        DRAINING {
            @Override
            protected State update(TileAirCompressor compressor, IFluidTankProperties properties, @Nullable FluidStack contents) {
                for (EnumFacing facing : OUTPUT_SIDES) {
                    TileEntity outputEntity = compressor.outputs.get(facing);

                    if (outputEntity == null || outputEntity.isInvalid()) {
                        outputEntity = compressor.world.getTileEntity(compressor.pos.offset(facing));
                        if (outputEntity == null) {
                            compressor.outputs.remove(facing);
                        } else {
                            compressor.outputs.put(facing, outputEntity);
                        }
                    }

                    if (outputEntity != null) {
                        IFluidHandler output = outputEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite());
                        if (output != null) {
                            FluidTransferUtils.transfer(compressor.outputStorage, output, DRAIN_PER_TICK);
                        }
                    }
                }

                FluidStack outputContents = compressor.outputStorage.getTankProperties()[0].getContents();
                if (outputContents == null || outputContents.amount <= 0) {
                    return State.FILLING;
                }

                return this;
            }
        };

        protected abstract State update(TileAirCompressor compressor, IFluidTankProperties properties, @Nullable FluidStack contents);
    }
}

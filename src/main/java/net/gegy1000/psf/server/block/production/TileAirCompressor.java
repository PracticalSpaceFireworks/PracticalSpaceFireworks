package net.gegy1000.psf.server.block.production;

import net.gegy1000.psf.server.fluid.PSFFluidRegistry;
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
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumMap;

@ParametersAreNonnullByDefault
public class TileAirCompressor extends TileEntity implements ITickable {
    private static final EnumFacing[] OUTPUT_SIDES = new EnumFacing[] { EnumFacing.UP, EnumFacing.DOWN };

    private static final int TOTAL_COMPRESSION_TIME = 120;

    private static final int TANK_SIZE = 1000;
    private static final int DRAIN_PER_TICK = 60;

    private static final int ENERGY_BUFFER = 6000;
    private static final int ENERGY_PER_TICK = 200;

    private final IFluidHandler fluidStorage = new FluidTank(TANK_SIZE);
    private final IEnergyStorage energyStorage = new EnergyStorage(ENERGY_BUFFER);

    private State state = State.FILLING;
    private int compressionTime;

    private EnumMap<EnumFacing, TileEntity> outputs = new EnumMap<>(EnumFacing.class);

    @Override
    public void update() {
        if (!world.isRemote) {
            IFluidTankProperties properties = fluidStorage.getTankProperties()[0];
            FluidStack tankContents = properties.getContents();
            switch (state) {
                case FILLING:
                    if (tankContents != null && tankContents.amount >= properties.getCapacity()) {
                        compressionTime = TOTAL_COMPRESSION_TIME;
                        state = State.COMPRESSING;
                    }
                    break;
                case COMPRESSING:
                    if (tankContents == null) {
                        state = State.FILLING;
                    }
                    if (energyStorage.extractEnergy(ENERGY_PER_TICK, false) >= ENERGY_PER_TICK) {
                        if (--compressionTime <= 0) {
                            state = State.DRAINING;
                        }
                    } else if (compressionTime < TOTAL_COMPRESSION_TIME) {
                        compressionTime++;
                    }
                    break;
                case DRAINING:
                    for (EnumFacing facing : OUTPUT_SIDES) {
                        TileEntity outputEntity = outputs.get(facing);

                        if (outputEntity == null || outputEntity.isInvalid()) {
                            outputEntity = world.getTileEntity(pos.offset(facing));
                            outputs.put(facing, outputEntity);
                        }

                        if (outputEntity != null) {
                            IFluidHandler output = outputEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite());
                            if (output != null) {
                                FluidStack drained = fluidStorage.drain(DRAIN_PER_TICK, true);
                                if (drained != null && drained.amount > 0) {
                                    output.fill(new FluidStack(PSFFluidRegistry.COMPRESSED_AIR, drained.amount), true);
                                } else {
                                    state = State.FILLING;
                                }
                            }
                        }
                    }

                    break;
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setTag("fluid", CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.writeNBT(fluidStorage, null));
        compound.setTag("energy", CapabilityEnergy.ENERGY.writeNBT(energyStorage, null));
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.readNBT(fluidStorage, null, compound.getTag("fluid"));
        CapabilityEnergy.ENERGY.readNBT(energyStorage, null, compound.getTag("energy"));
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
            if (facing != null && state != State.FILLING) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(EmptyFluidHandler.INSTANCE);
            }
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidStorage);
        }
        return super.getCapability(capability, facing);
    }

    private enum State {
        FILLING,
        COMPRESSING,
        DRAINING,
    }
}

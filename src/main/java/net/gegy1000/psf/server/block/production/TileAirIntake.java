package net.gegy1000.psf.server.block.production;

import net.gegy1000.psf.server.fluid.PSFFluidRegistry;
import net.minecraft.block.BlockDirectional;
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
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TileAirIntake extends TileEntity implements ITickable {
    private static final int ENERGY_BUFFER = 6000;
    private static final int ENERGY_PER_TICK = 40;

    private static final int AIR_PER_TICK = 100;

    private final IEnergyStorage energyStorage = new EnergyStorage(ENERGY_BUFFER);

    private EnumFacing facing;

    private TileEntity outputEntity;

    @Override
    public void update() {
        if (!world.isRemote && energyStorage.extractEnergy(ENERGY_PER_TICK, false) >= ENERGY_PER_TICK) {
            EnumFacing facing = getFacing();

            if (outputEntity == null || outputEntity.isInvalid()) {
                outputEntity = world.getTileEntity(pos.offset(facing.getOpposite()));
            }

            if (outputEntity != null) {
                IFluidHandler output = outputEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing);
                if (output != null) {
                    output.fill(new FluidStack(PSFFluidRegistry.FILTERED_AIR, AIR_PER_TICK), true);
                }
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setTag("energy", CapabilityEnergy.ENERGY.writeNBT(energyStorage, null));
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("energy")) {
            CapabilityEnergy.ENERGY.readNBT(energyStorage, null, compound.getTag("energy"));
        }
    }

    @Nonnull
    private EnumFacing getFacing() {
        if (facing == null) {
            facing = world.getBlockState(pos).getValue(BlockDirectional.FACING);
        }
        return facing;
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
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(EmptyFluidHandler.INSTANCE);
        }
        return super.getCapability(capability, facing);
    }
}

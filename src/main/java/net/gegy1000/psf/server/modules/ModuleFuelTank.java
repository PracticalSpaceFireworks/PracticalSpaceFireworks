package net.gegy1000.psf.server.modules;

import net.gegy1000.psf.api.IAdditionalMass;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.server.capability.CapabilityModuleData;
import net.gegy1000.psf.server.util.BlockMassHandler;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModuleFuelTank extends EmptyModule implements IModule {
    private static final int CAPACITY = 1000;

    private final FluidTank storage;
    private final FluidMass additionalMass;

    public ModuleFuelTank() {
        super("fuel_tank");
        this.storage = new FluidTank(FluidRegistry.WATER, CAPACITY, CAPACITY);
        this.additionalMass = new FluidMass(this.storage);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();

        Capability<IFluidHandler> cap = CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
        tag.setTag("fluid", cap.getStorage().writeNBT(cap, this.storage, null));

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        Capability<IFluidHandler> cap = CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
        cap.getStorage().readNBT(cap, this.storage, null, nbt.getTag("fluid"));
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return super.hasCapability(capability, facing) || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(storage);
        } else if (capability == CapabilityModuleData.ADDITIONAL_MASS) {
            return CapabilityModuleData.ADDITIONAL_MASS.cast(this.additionalMass);
        }
        return super.getCapability(capability, facing);
    }

    private class FluidMass implements IAdditionalMass {
        private final IFluidTank storage;
        private final double fluidDensity;

        private FluidMass(IFluidTank storage) {
            this.storage = storage;

            FluidStack stack = storage.getFluid();
            if (stack != null) {
                this.fluidDensity = stack.getFluid().getDensity() * 1000.0;
            } else {
                this.fluidDensity = BlockMassHandler.getMaterialMass(Material.WATER);
            }
        }

        @Override
        public double getAdditionalMass() {
            return (double) this.storage.getFluidAmount() / this.storage.getCapacity() * this.fluidDensity;
        }
    }
}

package net.gegy1000.psf.server.modules;

import lombok.experimental.Delegate;
import net.gegy1000.psf.api.IModule;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModuleFuelTank extends EmptyModule implements IModule {
    private static final int CAPACITY = 1000;

    @Delegate
    private final FluidTank storage;

    public ModuleFuelTank() {
        super("fuel_tank");
        this.storage = new FluidTank(FluidRegistry.WATER, CAPACITY, CAPACITY);
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
        if (hasCapability(capability, facing)) {
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(storage);
            }
        }
        return super.getCapability(capability, facing);
    }
}

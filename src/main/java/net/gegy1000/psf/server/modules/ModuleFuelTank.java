package net.gegy1000.psf.server.modules;

import lombok.experimental.Delegate;
import net.gegy1000.psf.api.IModule;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class ModuleFuelTank extends EmptyModule implements IModule, IFluidHandler, IFluidTank {
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
        tag.setTag("fluid", cap.getStorage().writeNBT(cap, this, null));

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        Capability<IFluidHandler> cap = CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
        cap.getStorage().readNBT(cap, this, null, nbt.getTag("fluid"));
    }
}

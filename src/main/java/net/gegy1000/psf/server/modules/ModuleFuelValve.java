package net.gegy1000.psf.server.modules;

import net.gegy1000.psf.api.module.IModule;
import net.gegy1000.psf.server.capability.MultiTankFluidHandler;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ModuleFuelValve extends EmptyModule {
    private IFluidHandler fuelHandler = EmptyFluidHandler.INSTANCE;

    public ModuleFuelValve() {
        super("fuel_valve");
    }

    @Override
    public void handleModuleChange(@Nonnull Collection<IModule> modules) {
        if (!modules.isEmpty()) {
            List<IFluidHandler> internal = new ArrayList<>();
            for (IModule module : modules) {
                if (module instanceof ModuleFuelValve) {
                    continue;
                }
                IFluidHandler fluidHandler = module.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
                if (fluidHandler != null) {
                    internal.add(fluidHandler);
                }
            }
            fuelHandler = new MultiTankFluidHandler(internal);
        } else {
            fuelHandler = EmptyFluidHandler.INSTANCE;
        }
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return super.hasCapability(capability, facing) || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fuelHandler);
        }
        return super.getCapability(capability, facing);
    }
}

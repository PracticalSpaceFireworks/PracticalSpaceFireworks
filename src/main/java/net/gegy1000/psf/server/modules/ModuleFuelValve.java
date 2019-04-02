package net.gegy1000.psf.server.modules;

import net.gegy1000.psf.api.module.IModule;
import net.gegy1000.psf.server.capability.MultiFluidHandler;
import net.gegy1000.psf.server.capability.MultiTankFluidHandler;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ModuleFuelValve extends ModuleFuelTank {
    private IFluidHandler fuelHandler = EmptyFluidHandler.INSTANCE;

    public ModuleFuelValve() {
        super("fuel_valve");
    }

    @Override
    public void handleModuleChange(@Nonnull Collection<IModule> modules) {
        if (!modules.isEmpty()) {
            fuelHandler = new MultiFluidHandler<>(collectFuelHandlers(modules));
        } else {
            fuelHandler = EmptyFluidHandler.INSTANCE;
        }
    }

    public Map<Fluid, FuelAmount> collectFuelAmounts() {
        Map<Fluid, FuelAmount> amounts = new HashMap<>();
        IFluidTankProperties[] tankProperties = fuelHandler.getTankProperties();
        for (IFluidTankProperties tank : tankProperties) {
            FluidStack contents = tank.getContents();
            if (contents != null) {
                FuelAmount quantity = amounts.computeIfAbsent(contents.getFluid(), fluid -> new FuelAmount());
                quantity.addAmount(contents.amount);
                quantity.addCapacity(tank.getCapacity());
            }
        }
        return amounts;
    }

    private Collection<IFluidHandler> collectFuelHandlers(@Nonnull Collection<IModule> modules) {
        Collection<IFluidHandler> fuelHandlers = new ArrayList<>();
        for (IModule module : modules) {
            IFluidHandler fluidHandler = module.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
            if (fluidHandler != null) {
                fuelHandlers.add(fluidHandler);
            }
        }
        return fuelHandlers;
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

package net.gegy1000.psf.server.modules;

import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.psf.api.module.IModule;
import net.gegy1000.psf.server.capability.MultiFluidHandler;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ModuleFuelValve extends EmptyModule {
    private IFluidHandler tanks = EmptyFluidHandler.INSTANCE;

    public ModuleFuelValve() {
        super("fuel_valve");
    }

    @Override
    public void handleModuleChange(Collection<IModule> modules) {
        if (modules.isEmpty()) {
            tanks = EmptyFluidHandler.INSTANCE;
        } else {
            tanks = new MultiFluidHandler<>(findFuelHandlers(modules));
        }
    }

    private Collection<IFluidHandler> findFuelHandlers( Collection<IModule> modules) {
        List<IFluidHandler> list = new ArrayList<>();
        for (IModule module : modules) {
            if (!(module instanceof ModuleFuelValve)) {
                IFluidHandler handler = module.getCapability(FLUID_HANDLER_CAPABILITY, null);
                if (handler != null) {
                    list.add(handler);
                }
            }
        }
        return list;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing side) {
        return super.hasCapability(capability, side) || FLUID_HANDLER_CAPABILITY == capability;
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing side) {
        if (FLUID_HANDLER_CAPABILITY == capability) {
            return FLUID_HANDLER_CAPABILITY.cast(tanks);
        }
        return super.getCapability(capability, side);
    }

    public Map<Fluid, FuelState> computeFuelStates() {
        Map<Fluid, FuelState> states = new HashMap<>();
        for (IFluidTankProperties properties : tanks.getTankProperties()) {
            FluidStack stack = properties.getContents();
            if (stack != null) {
                FuelState state = states.computeIfAbsent(stack.getFluid(), FuelState::new);
                state.addAmount(stack.amount).addCapacity(properties.getCapacity());
            }
        }
        return states;
    }
}

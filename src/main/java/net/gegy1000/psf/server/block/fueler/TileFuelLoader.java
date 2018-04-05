package net.gegy1000.psf.server.block.fueler;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.block.module.TileModule;
import net.gegy1000.psf.server.fluid.PSFFluidRegistry;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.FluidHandlerConcatenate;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public class TileFuelLoader extends TileEntity {
    private final IItemHandler inventory = new ItemStackHandler(NonNullList.withSize(2, ItemStack.EMPTY)) {
        @Override
        protected void onContentsChanged(int slot) {
            ItemStack stack = this.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
                IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                if (handler == null) {
                    return;
                }
                this.stacks.set(slot, fillFromItem(handler));
            }
        }
    };

    private IFluidHandler fluidHandler = null;

    public void rebuildTankList() {
        EnumFacing facing = world.getBlockState(pos).getValue(BlockHorizontal.FACING);

        Set<IFluidHandler> handlers = new HashSet<>();

        BlockPos origin = getPos().offset(facing);
        IModule module = TileModule.getModule(world.getTileEntity(origin));
        if (module != null) {
            ISatellite owner = module.getOwner();
            if (owner != null) {
                handlers.addAll(owner.getModules().stream()
                        .filter(m -> m.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null))
                        .map(m -> m.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null))
                        .filter(this::isFuelTank)
                        .collect(Collectors.toList()));
            }
        }

        fluidHandler = new FluidHandlerConcatenate(handlers);
    }

    public Map<Fluid, FuelAmount> collectFuelAmounts() {
        Map<Fluid, FuelAmount> amounts = new HashMap<>();
        if (fluidHandler != null) {
            IFluidTankProperties[] tankProperties = fluidHandler.getTankProperties();
            for (IFluidTankProperties tank : tankProperties) {
                FluidStack contents = tank.getContents();
                if (contents != null) {
                    FuelAmount amount = amounts.computeIfAbsent(contents.getFluid(), fluid -> new FuelAmount());
                    amount.amount += contents.amount;
                    amount.capacity += tank.getCapacity();
                }
            }
        }
        return amounts;
    }

    private ItemStack fillFromItem(IFluidHandlerItem handler) {
        if (fluidHandler == null) {
            rebuildTankList();
        }

        IFluidTankProperties[] tankProperties = handler.getTankProperties();
        for (IFluidTankProperties properties : tankProperties) {
            FluidStack contents = properties.getContents();
            if (contents != null) {
                int filled = fluidHandler.fill(handler.drain(contents, true), true);
                if (filled < contents.amount) {
                    handler.fill(new FluidStack(contents.getFluid(), filled), true);
                }
            }
        }

        return handler.getContainer();
    }

    private boolean isFuelTank(IFluidHandler handler) {
        IFluidTankProperties[] tankProperties = handler.getTankProperties();
        for (IFluidTankProperties properties : tankProperties) {
            FluidStack contents = properties.getContents();
            if (isAcceptableFluid(contents)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAcceptableFluid(@Nullable FluidStack stack) {
        return stack != null && (stack.getFluid() == PSFFluidRegistry.KEROSENE || stack.getFluid() == PSFFluidRegistry.LIQUID_OXYGEN);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ||
                capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY ||
                super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            if (fluidHandler == null) {
                rebuildTankList();
            }
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler);
        }
        return super.getCapability(capability, facing);
    }

    @EqualsAndHashCode
    public static class FuelAmount {
        @Getter
        @Setter
        private int amount;
        @Getter
        @Setter
        private int capacity;
    }
}

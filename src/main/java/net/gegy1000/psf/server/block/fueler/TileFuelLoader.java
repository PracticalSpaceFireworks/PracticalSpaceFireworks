package net.gegy1000.psf.server.block.fueler;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.gegy1000.psf.server.block.PSFBlockRegistry;
import net.gegy1000.psf.server.block.controller.TileController;
import net.gegy1000.psf.server.capability.CapabilityModule;
import net.gegy1000.psf.server.fluid.PSFFluidRegistry;
import net.gegy1000.psf.server.util.ContiguousBlockIterator;
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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
                IFluidTankProperties[] tankProperties = handler.getTankProperties();
                for (IFluidTankProperties properties : tankProperties) {
                    FluidStack contents = properties.getContents();
                    if (contents != null) {
                        if (fluidHandler == null) {
                            rebuildTankList();
                        }
                        int filled = fluidHandler.fill(handler.drain(contents, true), true);
                        if (filled < contents.amount) {
                            handler.fill(new FluidStack(contents.getFluid(), filled), true);
                        }
                        this.stacks.set(slot, handler.getContainer());
                    }
                }
            }
        }
    };

    private IFluidHandler fluidHandler = null;

    public void rebuildTankList() {
        EnumFacing facing = PSFBlockRegistry.fuelLoader.getStateFromMeta(getBlockMetadata()).getValue(BlockHorizontal.FACING);

        Set<IFluidHandler> handlers = new HashSet<>();

        BlockPos origin = getPos().offset(facing);
        if (getFuelTank(origin) != null) {
            Iterator<BlockPos> tankIterator = getContiguousTankIterator(origin);
            while (tankIterator.hasNext()) {
                BlockPos pos = tankIterator.next();

                IFluidHandler fuelTank = getFuelTank(pos);
                if (fuelTank != null) {
                    handlers.add(fuelTank);
                }
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

    private Iterator<BlockPos> getContiguousTankIterator(BlockPos origin) {
        return new ContiguousBlockIterator(origin, TileController.CONTIGUOUS_RANGE, pos -> getFuelTank(pos) != null);
    }

    @Nullable
    private IFluidHandler getFuelTank(BlockPos pos) {
        TileEntity entity = world.getTileEntity(pos);
        if (entity != null && entity.hasCapability(CapabilityModule.INSTANCE, null) && entity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
            IFluidHandler fluidHandler = entity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
            IFluidTankProperties[] tankProperties = fluidHandler.getTankProperties();
            for (IFluidTankProperties properties : tankProperties) {
                FluidStack contents = properties.getContents();
                if (isAcceptableFluid(contents)) {
                    return fluidHandler;
                }
            }
        }
        return null;
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

package net.gegy1000.psf.server.block.valve;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class SlotFluidContainer extends SlotItemHandler {
    private final Fluid fluidType;

    public SlotFluidContainer(IItemHandler itemHandler, int index, int posX, int posY, Fluid fluidType) {
        super(itemHandler, index, posX, posY);
        this.fluidType = fluidType;
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        if (stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            IFluidHandler fluidHandler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            IFluidTankProperties[] tankProperties = fluidHandler.getTankProperties();
            if (tankProperties.length == 1) {
                IFluidTankProperties properties = tankProperties[0];
                FluidStack contents = properties.getContents();
                return contents != null && contents.getFluid() == this.fluidType;
            }
        }
        return false;
    }
}

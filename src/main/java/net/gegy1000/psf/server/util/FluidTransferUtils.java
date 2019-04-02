package net.gegy1000.psf.server.util;

import lombok.val;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY;

public class FluidTransferUtils {
    public static boolean extractIntoHeldItem(World world, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing facing) {
        @Nullable val te = world.getTileEntity(pos);
        if (te == null) return false;
        @Nullable val source = te.getCapability(FLUID_HANDLER_CAPABILITY, facing);
        if (source == null) return false;

        ItemStack heldItem = player.getHeldItem(hand);
        IFluidHandlerItem fluidItem = heldItem.getCapability(FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidItem != null) {
            if (world.isRemote) return true;

            for (IFluidTankProperties properties : fluidItem.getTankProperties()) {
                int capacity = properties.getCapacity();
                FluidTransferUtils.transfer(source, fluidItem, capacity);
            }
            player.setHeldItem(hand, fluidItem.getContainer());

            return true;
        }

        return false;
    }

    public static int transfer(IFluidHandler source, IFluidHandler target, int amount) {
        FluidStack drained = source.drain(amount, true);
        if (drained != null && drained.amount > 0) {
            int filled = target.fill(drained, true);
            FluidStack remaining = new FluidStack(drained.getFluid(), drained.amount - filled);
            if (remaining.amount > 0) {
                source.fill(remaining, true);
            }
            return filled;
        }
        return 0;
    }
}

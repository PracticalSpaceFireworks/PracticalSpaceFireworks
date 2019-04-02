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
    public static boolean transferWithHeldItem(World world, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing facing) {
        @Nullable val te = world.getTileEntity(pos);
        if (te == null) return false;
        @Nullable val source = te.getCapability(FLUID_HANDLER_CAPABILITY, facing);
        if (source == null) return false;

        ItemStack heldItem = takeHeld(player, hand);

        IFluidHandlerItem fluidItem = heldItem.getCapability(FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidItem != null) {
            if (world.isRemote) return true;

            boolean transferredInto = transferIntoItem(source, fluidItem);
            if (!transferredInto) {
                transferFromItem(source, fluidItem);
            }

            InventoryUtils.returnStack(player, hand, fluidItem.getContainer());

            return true;
        }

        return false;
    }

    private static boolean transferIntoItem(IFluidHandler source, IFluidHandlerItem fluidItem) {
        boolean transferred = false;
        for (IFluidTankProperties properties : fluidItem.getTankProperties()) {
            int capacity = properties.getCapacity();
            if (FluidTransferUtils.transfer(source, fluidItem, capacity) > 0) {
                transferred = true;
            }
        }
        return transferred;
    }

    private static boolean transferFromItem(IFluidHandler source, IFluidHandlerItem fluidItem) {
        boolean transferred = false;
        for (IFluidTankProperties properties : source.getTankProperties()) {
            int capacity = properties.getCapacity();
            if (FluidTransferUtils.transfer(fluidItem, source, capacity) > 0) {
                transferred = true;
            }
        }
        return transferred;
    }

    private static ItemStack takeHeld(EntityPlayer player, EnumHand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        ItemStack taken = heldItem.copy();

        heldItem.shrink(1);
        taken.setCount(1);

        player.setHeldItem(hand, heldItem);

        return taken;
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

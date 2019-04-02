package net.gegy1000.psf.server.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public final class InventoryUtils {
    public static void returnStack(EntityPlayer player, EnumHand hand, ItemStack stack) {
        if (player.getHeldItem(hand).isEmpty()) {
            player.setHeldItem(hand, stack);
        } else if (!player.addItemStackToInventory(stack)) {
            player.dropItem(stack, false);
        }
    }

    public static ItemStack extractHeld(EntityPlayer player, EnumHand hand, int count) {
        ItemStack heldItem = player.getHeldItem(hand);
        ItemStack taken = heldItem.copy();

        heldItem.shrink(count);
        taken.setCount(count);

        player.setHeldItem(hand, heldItem);

        return taken;
    }
}

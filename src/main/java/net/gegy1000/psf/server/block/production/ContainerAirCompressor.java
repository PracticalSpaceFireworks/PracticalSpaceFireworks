package net.gegy1000.psf.server.block.production;

import lombok.Getter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class ContainerAirCompressor extends Container {
    public static final int AIR_AMOUNT = 0;
    public static final int COMPRESSED_AIR_AMOUNT = 1;
    public static final int ACTIVE = 2;

    private static final int PLAYER_INVENTORY_SIZE = 36;

    private final TileAirCompressor te;
    private final IFluidHandler fluidHandler;

    @Getter
    private int airAmount;
    @Getter
    private int compressedAirAmount;
    @Getter
    private boolean active;

    public ContainerAirCompressor(TileAirCompressor te, InventoryPlayer playerInventory) {
        this.te = te;
        this.fluidHandler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlotToContainer(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlotToContainer(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        FluidStack air = fluidHandler.getTankProperties()[0].getContents();
        FluidStack compressedAir = fluidHandler.getTankProperties()[1].getContents();
        int airAmount = air == null ? 0 : air.amount;
        int compressedAirAmount = compressedAir == null ? 0 : compressedAir.amount;
        boolean active = te.getState() == TileAirCompressor.COMPRESSING_STATE;

        boolean airChanged = airAmount != this.airAmount;
        boolean compressedAirChanged = compressedAirAmount != this.compressedAirAmount;
        boolean activeChanged = active != this.active;

        for (IContainerListener listener : this.listeners) {
            if (airChanged) {
                listener.sendWindowProperty(this, AIR_AMOUNT, airAmount);
            }
            if (compressedAirChanged) {
                listener.sendWindowProperty(this, COMPRESSED_AIR_AMOUNT, compressedAirAmount);
            }
            if (activeChanged) {
                listener.sendWindowProperty(this, ACTIVE, active ? 1 : 0);
            }
        }
        
        this.airAmount = airAmount;
        this.compressedAirAmount = compressedAirAmount;
        this.active = active;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        super.updateProgressBar(id, data);

        switch (id) {
            case AIR_AMOUNT:
                this.airAmount = data;
                break;
            case COMPRESSED_AIR_AMOUNT:
                this.compressedAirAmount = data;
                break;
            case ACTIVE:
                this.active = data == 1;
                break;
        }
    }

    @Override
    @Nonnull
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {

        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < 27) {
                if (!this.mergeItemStack(itemstack1, 27, 36, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 0, 27, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            ItemStack itemstack2 = slot.onTake(player, itemstack1);

            if (index == 0) {
                player.dropItem(itemstack2, false);
            }
        }

        return itemstack;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}

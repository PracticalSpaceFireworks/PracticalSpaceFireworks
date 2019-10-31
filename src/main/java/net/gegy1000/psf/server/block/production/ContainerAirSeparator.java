package net.gegy1000.psf.server.block.production;

import javax.annotation.Nonnull;

import java.util.Map;

import lombok.Getter;
import net.gegy1000.psf.server.block.valve.SlotFluidContainer;
import net.gegy1000.psf.server.init.PSFFluids;
import net.gegy1000.psf.server.modules.FuelState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class ContainerAirSeparator extends Container {
    public static final int COMPRESSED_AIR_AMOUNT = 0;
    public static final int COMPRESSED_AIR_CAPACITY = 1;
    public static final int LIQUID_NITROGEN_AMOUNT = 2;
    public static final int LIQUID_NITROGEN_CAPACITY = 3;
    public static final int LIQUID_OXYGEN_AMOUNT = 4;
    public static final int LIQUID_OXYGEN_CAPACITY = 5;
    public static final int ACTIVE = 6;

    private static final int PLAYER_INVENTORY_SIZE = 36;

    private final TileAirSeparator te;

    private final IItemHandler inputHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            this.stacks.set(slot, drainToInput(slot == 0 ? te.getNitrogenTank() : te.getOxygenTank(), getStackInSlot(slot)));
            detectAndSendChanges();
        }
        
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };

    @Getter
    private FuelState compressedAirState = new FuelState();
    @Getter
    private FuelState liquidNitrogenState = new FuelState();
    @Getter
    private FuelState liquidOxygenState = new FuelState();
    @Getter
    private boolean active;

    public ContainerAirSeparator(TileAirSeparator te, InventoryPlayer playerInventory) {
        this.te = te;

        this.addSlotToContainer(new SlotFluidContainer(this.inputHandler, 0, 8, 37, null));
        this.addSlotToContainer(new SlotFluidContainer(this.inputHandler, 1, 152, 37, null));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlotToContainer(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlotToContainer(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
    }

    private ItemStack drainToInput(IFluidHandler from, ItemStack stack) {
        FluidActionResult res = FluidUtil.tryFillContainer(stack, from, 1000, null, true);
        if (res.isSuccess()) {
            return res.getResult();
        }
        return stack;
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if (!te.getWorld().isRemote) {
            boolean drop = !player.isEntityAlive() || player instanceof EntityPlayerMP && ((EntityPlayerMP) player).hasDisconnected();
            for (int i = 0; i < inputHandler.getSlots(); i++) {
                ItemStack stack = inputHandler.getStackInSlot(i);
                if (drop) {
                    player.dropItem(stack, false);
                } else {
                    player.inventory.placeItemBackInInventory(te.getWorld(), stack);
                }
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (te.getWorld().isRemote) {
            return;
        }

        Map<Fluid, FuelState> fuelStates = te.computeFuelStates();
        FuelState compressedAirState = fuelStates.getOrDefault(PSFFluids.COMPRESSED_AIR.getFluid(), new FuelState());
        FuelState liquidNitrogenState = fuelStates.getOrDefault(PSFFluids.LIQUID_NITROGEN.getFluid(), new FuelState());
        FuelState liquidOxygenState = fuelStates.getOrDefault(PSFFluids.LIQUID_OXYGEN.getFluid(), new FuelState());
        boolean active = te.isActive();

        boolean compressedAirChanged = !compressedAirState.equals(this.compressedAirState);
        boolean liquidNitrogenChanged = liquidNitrogenState != this.liquidNitrogenState;
        boolean liquidOxygenChanged = liquidOxygenState != this.liquidOxygenState;
        boolean activeChanged = active != this.active;

        for (IContainerListener listener : this.listeners) {
            if (compressedAirChanged) {
                listener.sendWindowProperty(this, COMPRESSED_AIR_AMOUNT, compressedAirState.getAmount());
                listener.sendWindowProperty(this, COMPRESSED_AIR_CAPACITY, compressedAirState.getCapacity());
            }
            if (liquidNitrogenChanged) {
                listener.sendWindowProperty(this, LIQUID_NITROGEN_AMOUNT, liquidNitrogenState.getAmount());
                listener.sendWindowProperty(this, LIQUID_NITROGEN_CAPACITY, liquidNitrogenState.getCapacity());
            }
            if (liquidOxygenChanged) {
                listener.sendWindowProperty(this, LIQUID_OXYGEN_AMOUNT, liquidOxygenState.getAmount());
                listener.sendWindowProperty(this, LIQUID_OXYGEN_CAPACITY, liquidOxygenState.getCapacity());
            }
            if (activeChanged) {
                listener.sendWindowProperty(this, ACTIVE, active ? 1 : 0);
            }
        }
        
        this.compressedAirState = compressedAirState;
        this.liquidNitrogenState = liquidNitrogenState;
        this.liquidOxygenState = liquidOxygenState;
        this.active = active;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        super.updateProgressBar(id, data);

        switch (id) {
            case COMPRESSED_AIR_AMOUNT:
                this.compressedAirState.setAmount(data);;
                break;
            case COMPRESSED_AIR_CAPACITY:
                this.compressedAirState.setCapacity(data);
                break;
            case LIQUID_NITROGEN_AMOUNT:
                this.liquidNitrogenState.setAmount(data);
                break;
            case LIQUID_NITROGEN_CAPACITY:
                this.liquidNitrogenState.setCapacity(data);
                break;
            case LIQUID_OXYGEN_AMOUNT:
                this.liquidOxygenState.setAmount(data);
                break;
            case LIQUID_OXYGEN_CAPACITY:
                this.liquidOxygenState.setCapacity(data);
                break;
            case ACTIVE:
                this.active = data == 1;
                break;
        }
    }

    @Override
    @Nonnull
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack transferred = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        int otherSlots = this.inventorySlots.size() - PLAYER_INVENTORY_SIZE;
        if (slot != null && slot.getHasStack()) {
            ItemStack current = slot.getStack();
            ItemStack copy = current.copy();
            transferred = current.copy();
            if (index < otherSlots) {
                if (!this.mergeItemStack(copy, otherSlots, this.inventorySlots.size(), false)) {
                    return ItemStack.EMPTY;
                } else {
                    slot.onSlotChanged();
                }
            } else if (!this.mergeItemStack(copy, 0, otherSlots, false)) {
                return ItemStack.EMPTY;
            }
            if (copy.getCount() == 0) {
                slot.onTake(player, copy);
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.putStack(copy);
                slot.onSlotChanged();
            }
        }
        return transferred;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}

package net.gegy1000.psf.server.block.production;

import lombok.Getter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ContainerKeroseneExtractor extends Container {
    private static final int KEROSENE_AMOUNT = 0;
    private static final int EXTRACTION_TIME = 1;
    private static final int EXTRACTION_AMOUNT = 2;
    private static final int ACTIVE = 3;

    private static final int PLAYER_INVENTORY_SIZE = 36;

    private final TileKeroseneExtractor entity;

    @Getter
    private int keroseneAmount;
    @Getter
    private int extractionTime;
    @Getter
    private int extractionAmount;
    @Getter
    private boolean active;

    public ContainerKeroseneExtractor(TileKeroseneExtractor entity, InventoryPlayer playerInventory) {
        this.entity = entity;

        IItemHandler inputHandler = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        this.addSlotToContainer(new SlotItemHandler(inputHandler, 0, 49, 30));

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

        boolean keroseneChanged = entity.getKeroseneAmount() != keroseneAmount;
        boolean timeChanged = entity.getExtractionTime() != extractionTime;
        boolean amountChanged = entity.getTotalExtractionAmount() != extractionAmount;
        boolean activeChanged = entity.isActive() != active;

        for (IContainerListener listener : this.listeners) {
            if (keroseneChanged) {
                listener.sendWindowProperty(this, KEROSENE_AMOUNT, entity.getKeroseneAmount());
            }
            if (timeChanged) {
                listener.sendWindowProperty(this, EXTRACTION_TIME, entity.getExtractionTime());
            }
            if (amountChanged) {
                listener.sendWindowProperty(this, EXTRACTION_AMOUNT, entity.getTotalExtractionAmount());
            }
            if (activeChanged) {
                listener.sendWindowProperty(this, ACTIVE, entity.isActive() ? 1 : 0);
            }
        }

        keroseneAmount = entity.getKeroseneAmount();
        extractionTime = entity.getExtractionTime();
        extractionAmount = entity.getTotalExtractionAmount();
        active = entity.isActive();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        super.updateProgressBar(id, data);

        switch (id) {
            case KEROSENE_AMOUNT:
                this.keroseneAmount = data;
                break;
            case EXTRACTION_TIME:
                this.extractionTime = data;
                break;
            case EXTRACTION_AMOUNT:
                this.extractionAmount = data;
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

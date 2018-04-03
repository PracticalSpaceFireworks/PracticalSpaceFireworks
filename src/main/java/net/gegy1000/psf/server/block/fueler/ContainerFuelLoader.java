package net.gegy1000.psf.server.block.fueler;

import lombok.Getter;
import net.gegy1000.psf.server.fluid.PSFFluidRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@ParametersAreNonnullByDefault
public class ContainerFuelLoader extends Container {
    public static final int KEROSENE_AMOUNT = 0;
    public static final int KEROSENE_CAPACITY = 1;

    public static final int LIQUID_OXYGEN_AMOUNT = 2;
    public static final int LIQUID_OXYGEN_CAPACITY = 3;

    private static final int PLAYER_INVENTORY_SIZE = 36;

    private final TileFuelLoader entity;

    @Getter
    private TileFuelLoader.FuelAmount keroseneAmount = new TileFuelLoader.FuelAmount();
    @Getter
    private TileFuelLoader.FuelAmount liquidOxygenAmount = new TileFuelLoader.FuelAmount();

    public ContainerFuelLoader(TileFuelLoader entity, InventoryPlayer playerInventory) {
        this.entity = entity;

        IItemHandler itemHandler = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        this.addSlotToContainer(new SlotFluidContainer(itemHandler, 0, 26, 36, PSFFluidRegistry.KEROSENE));
        this.addSlotToContainer(new SlotFluidContainer(itemHandler, 1, 134, 36, PSFFluidRegistry.LIQUID_OXYGEN));

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

        Map<Fluid, TileFuelLoader.FuelAmount> fuelAmounts = entity.collectFuelAmounts();
        TileFuelLoader.FuelAmount keroseneAmount = fuelAmounts.getOrDefault(PSFFluidRegistry.KEROSENE, new TileFuelLoader.FuelAmount());
        TileFuelLoader.FuelAmount liquidOxygenAmount = fuelAmounts.getOrDefault(PSFFluidRegistry.LIQUID_OXYGEN, new TileFuelLoader.FuelAmount());

        boolean keroseneChanged = !this.keroseneAmount.equals(keroseneAmount);
        boolean liquidOxygenChanged = !this.liquidOxygenAmount.equals(liquidOxygenAmount);

        for (IContainerListener listener : this.listeners) {
            if (keroseneChanged) {
                listener.sendWindowProperty(this, KEROSENE_AMOUNT, keroseneAmount.getAmount());
                listener.sendWindowProperty(this, KEROSENE_CAPACITY, keroseneAmount.getCapacity());
            }
            if (liquidOxygenChanged) {
                listener.sendWindowProperty(this, LIQUID_OXYGEN_AMOUNT, liquidOxygenAmount.getAmount());
                listener.sendWindowProperty(this, LIQUID_OXYGEN_CAPACITY, liquidOxygenAmount.getCapacity());
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        super.updateProgressBar(id, data);

        if (this.keroseneAmount == null) {
            this.keroseneAmount = new TileFuelLoader.FuelAmount();
        }
        if (this.liquidOxygenAmount == null) {
            this.liquidOxygenAmount = new TileFuelLoader.FuelAmount();
        }

        switch (id) {
            case KEROSENE_AMOUNT:
                this.keroseneAmount.setAmount(data);
                break;
            case KEROSENE_CAPACITY:
                this.keroseneAmount.setCapacity(data);
                break;
            case LIQUID_OXYGEN_AMOUNT:
                this.liquidOxygenAmount.setAmount(data);
                break;
            case LIQUID_OXYGEN_CAPACITY:
                this.liquidOxygenAmount.setCapacity(data);
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
        return player.getDistanceSqToCenter(entity.getPos()) < 64.0;
    }
}

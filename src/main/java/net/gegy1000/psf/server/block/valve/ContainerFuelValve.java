package net.gegy1000.psf.server.block.valve;

import lombok.Getter;
import net.gegy1000.psf.server.init.PSFFluids;
import net.gegy1000.psf.server.modules.FuelState;
import net.gegy1000.psf.server.modules.ModuleFuelValve;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class ContainerFuelValve extends Container {
    public static final int KEROSENE_AMOUNT = 0;
    public static final int KEROSENE_CAPACITY = 1;

    public static final int LIQUID_OXYGEN_AMOUNT = 2;
    public static final int LIQUID_OXYGEN_CAPACITY = 3;

    private static final int PLAYER_INVENTORY_SIZE = 36;

    private final World world;
    private final ModuleFuelValve module;
    private final IFluidHandler fluidHandler;

    private final IItemHandler inputHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            this.stacks.set(slot, fillFromInput(getStackInSlot(slot)));
            detectAndSendChanges();
        }
    };

    @Getter
    private FuelState keroseneState = new FuelState();
    @Getter
    private FuelState liquidOxygenState = new FuelState();

    public ContainerFuelValve(World world, ModuleFuelValve module, InventoryPlayer playerInventory) {
        this.world = world;
        this.module = module;

        this.fluidHandler = module.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);

        this.addSlotToContainer(new SlotFluidContainer(this.inputHandler, 0, 26, 36, PSFFluids.kerosene()));
        this.addSlotToContainer(new SlotFluidContainer(this.inputHandler, 1, 134, 36, PSFFluids.liquidOxygen()));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlotToContainer(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlotToContainer(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
    }

    private ItemStack fillFromInput(ItemStack stack) {
        IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (handler == null) return stack;

        Stream<FluidStack> allContents = Arrays.stream(handler.getTankProperties())
                .map(IFluidTankProperties::getContents)
                .filter(Objects::nonNull);

        allContents.forEach(contents -> {
            int originalAmount = contents.amount;
            int filled = fluidHandler.fill(handler.drain(contents, true), true);
            if (filled < originalAmount) {
                handler.fill(new FluidStack(contents.getFluid(), originalAmount - filled), true);
            }
        });

        return handler.getContainer();
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if (!world.isRemote) {
            boolean drop = !player.isEntityAlive() || player instanceof EntityPlayerMP && ((EntityPlayerMP) player).hasDisconnected();
            for (int i = 0; i < inputHandler.getSlots(); i++) {
                ItemStack stack = inputHandler.getStackInSlot(i);
                if (drop) {
                    player.dropItem(stack, false);
                } else {
                    player.inventory.placeItemBackInInventory(world, stack);
                }
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        Map<Fluid, FuelState> states = module.computeFuelStates();
        FuelState keroseneState = states.getOrDefault(PSFFluids.kerosene(), new FuelState());
        FuelState liquidOxygenState = states.getOrDefault(PSFFluids.liquidOxygen(), new FuelState());

        boolean keroseneChanged = !this.keroseneState.equals(keroseneState);
        boolean liquidOxygenChanged = !this.liquidOxygenState.equals(liquidOxygenState);

        for (IContainerListener listener : this.listeners) {
            if (keroseneChanged) {
                listener.sendWindowProperty(this, KEROSENE_AMOUNT, keroseneState.getAmount());
                listener.sendWindowProperty(this, KEROSENE_CAPACITY, keroseneState.getCapacity());
            }
            if (liquidOxygenChanged) {
                listener.sendWindowProperty(this, LIQUID_OXYGEN_AMOUNT, liquidOxygenState.getAmount());
                listener.sendWindowProperty(this, LIQUID_OXYGEN_CAPACITY, liquidOxygenState.getCapacity());
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        super.updateProgressBar(id, data);

        if (this.keroseneState == null) {
            this.keroseneState = new FuelState();
        }
        if (this.liquidOxygenState == null) {
            this.liquidOxygenState = new FuelState();
        }

        switch (id) {
            case KEROSENE_AMOUNT:
                this.keroseneState.setAmount(data);
                break;
            case KEROSENE_CAPACITY:
                this.keroseneState.setCapacity(data);
                break;
            case LIQUID_OXYGEN_AMOUNT:
                this.liquidOxygenState.setAmount(data);
                break;
            case LIQUID_OXYGEN_CAPACITY:
                this.liquidOxygenState.setCapacity(data);
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

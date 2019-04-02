package net.gegy1000.psf.server.block.production;

import lombok.Getter;
import net.gegy1000.psf.server.init.PSFFluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileKeroseneExtractor extends TileEntity implements ITickable {

    private static final int ENERGY_BUFFER = 5000;
    private static final int ENERGY_PER_TICK = 40;

    public static final int MAX_STORAGE = 1000;

    @Getter
    private int extractionTime;
    @Getter
    private int totalExtractionAmount = -1;

    private final FluidTank fluidTank = new FluidTank(PSFFluids.kerosene(), 0, MAX_STORAGE);

    private final IItemHandler itemHandler = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return getFuelAmount(stack) > 0;
        }
    };

    private final MachineEnergyStorage energyStorage = new MachineEnergyStorage(this, ENERGY_BUFFER);

    @Override
    public void update() {
        if (world.isRemote) return;

        boolean hasEnergy = energyStorage.extractEnergy(ENERGY_PER_TICK, true) >= ENERGY_PER_TICK;

        if (hasEnergy) {
            if (!isExtracting()) {
                beginExtracting();
            } else {
                energyStorage.extractEnergy(ENERGY_PER_TICK, false);
                if (++extractionTime > totalExtractionAmount) {
                    extractionTime = 0;
                    fluidTank.fill(new FluidStack(PSFFluids.kerosene(), totalExtractionAmount), true);
                    beginExtracting();
                }
            }
        }
    }
    
    private void beginExtracting() {
        ItemStack extractedStack = itemHandler.extractItem(0, 1, true);
        if (!extractedStack.isEmpty()) {
            extractionTime = 0;
            int space = fluidTank.getCapacity() - fluidTank.getFluidAmount();
            int fuel = getFuelAmount(extractedStack);
            if (fuel <= space) {
                itemHandler.extractItem(0, 1, false);
                totalExtractionAmount = fuel;
                return;
            }
        }
        totalExtractionAmount = -1;
    }

    private boolean isExtracting() {
        return totalExtractionAmount > 0;
    }

    public boolean isActive() {
        return energyStorage.isActive() && isExtracting();
    }

    public int getKeroseneAmount() {
        return fluidTank.getFluidAmount();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setTag("energy", CapabilityEnergy.ENERGY.writeNBT(energyStorage, null));
        compound.setTag("inventory", CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.writeNBT(itemHandler, null));
        compound.setTag("fluid", CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.writeNBT(fluidTank, null));
        compound.setInteger("extraction_time", extractionTime);
        compound.setInteger("total_extraction_amount", totalExtractionAmount);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("energy")) {
            CapabilityEnergy.ENERGY.readNBT(energyStorage, null, compound.getTag("energy"));
        }
        if (compound.hasKey("fluid")) {
            CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.readNBT(fluidTank, null, compound.getTag("fluid"));
        }
        if (compound.hasKey("inventory")) {
            CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.readNBT(itemHandler, null, compound.getTag("inventory"));
        }
        this.extractionTime = compound.getInteger("extraction_time");
        this.totalExtractionAmount = compound.getInteger("total_extraction_amount");
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return super.hasCapability(capability, facing)
                || capability == CapabilityEnergy.ENERGY
                || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
                || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
    }

    private static int getFuelAmount(ItemStack stack) {
        for (int id : OreDictionary.getOreIDs(stack)) {
            String name = OreDictionary.getOreName(id);
            switch (name) {
                case "logWood":
                    return 10;
                case "itemCharcoal":
                    return 15;
                case "itemCoal":
                    return 30;
            }
        }
        return 0;
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(energyStorage);
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidTank);
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler);
        }
        return super.getCapability(capability, facing);
    }
}

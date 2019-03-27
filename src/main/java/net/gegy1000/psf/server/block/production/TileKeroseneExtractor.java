package net.gegy1000.psf.server.block.production;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.gegy1000.psf.server.init.PSFFluids;
import net.minecraft.block.BlockDirectional;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;

public class TileKeroseneExtractor extends TileEntity implements ITickable {
    
    private static final int ENERGY_BUFFER = 5000;
    private static final int ENERGY_PER_TICK = 40;
    
    private static final int MAX_STORAGE = 1000;

    private final IEnergyStorage energyStorage = new EnergyStorage(ENERGY_BUFFER);
    
    private int storedInput;
    private int storedOutput;

    private EnumFacing facing;

    @Override
    public void update() {
        if (!world.isRemote && storedInput > 0 && storedOutput < MAX_STORAGE && energyStorage.extractEnergy(ENERGY_PER_TICK, false) >= ENERGY_PER_TICK) {
            storedInput--;
            storedOutput++;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setTag("energy", CapabilityEnergy.ENERGY.writeNBT(energyStorage, null));
        compound.setInteger("input", storedInput);
        compound.setInteger("output", storedOutput);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("energy")) {
            CapabilityEnergy.ENERGY.readNBT(energyStorage, null, compound.getTag("energy"));
        }
        this.storedInput = compound.getInteger("input");
        this.storedOutput = compound.getInteger("output");
    }

    @Nonnull
    private EnumFacing getFacing() {
        if (facing == null) {
            facing = world.getBlockState(pos).getValue(BlockDirectional.FACING);
        }
        return facing;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return super.hasCapability(capability, facing)
                || capability == CapabilityEnergy.ENERGY
                || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
                || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
    }
    
    private int getFuelAmount(ItemStack stack) {
        for (int id : OreDictionary.getOreIDs(stack)) {
            String name = OreDictionary.getOreName(id);
            if (name.equals("logWood")) {
                return 10;
            } else if (name.equals("itemCharcoal")) {
                return 15;
            } else if (name.equals("itemCoal")) {
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
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new IFluidHandler() {
                
                @Override
                public IFluidTankProperties[] getTankProperties() {
                    return new IFluidTankProperties[0];
                }
                
                @Override
                public int fill(FluidStack resource, boolean doFill) {
                    return 0;
                }
                
                @Override
                @Nullable
                public FluidStack drain(int maxDrain, boolean doDrain) {
                    FluidStack ret = new FluidStack(PSFFluids.kerosene(), Math.min(storedOutput, maxDrain));
                    if (doDrain) {
                        storedOutput -= ret.amount;
                    }
                    return ret.amount == 0 ? null : ret;
                }
                
                @Override
                @Nullable
                public FluidStack drain(FluidStack resource, boolean doDrain) {
                    if (resource.getFluid() == PSFFluids.kerosene()) {
                        return drain(resource.amount, doDrain);
                    }
                    return null;
                }
            });
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new IItemHandler() {
                
                @Override
                @Nonnull
                public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                    int amount = getFuelAmount(stack);
                    if (amount > 0) {
                        int toAdd = stack.getCount() * amount;
                        toAdd = Math.min(toAdd, MAX_STORAGE - storedInput);
                        toAdd /= amount;
                        if (!simulate) {
                            storedInput += toAdd * amount;
                        }
                        if (toAdd == stack.getCount()) {
                            return ItemStack.EMPTY;
                        }
                        ItemStack ret = stack.copy();
                        ret.setCount(stack.getCount() - toAdd);
                        return ret;
                    }
                    return ItemStack.EMPTY;
                }
                
                @Override
                @Nonnull
                public ItemStack getStackInSlot(int slot) {
                    return ItemStack.EMPTY;
                }
                
                @Override
                public int getSlots() {
                    return 1;
                }
                
                @Override
                public int getSlotLimit(int slot) {
                    return 1;
                }
                
                @Override
                @Nonnull
                public ItemStack extractItem(int slot, int amount, boolean simulate) {
                    return ItemStack.EMPTY;
                }
            });
        }
        return super.getCapability(capability, facing);
    }
}

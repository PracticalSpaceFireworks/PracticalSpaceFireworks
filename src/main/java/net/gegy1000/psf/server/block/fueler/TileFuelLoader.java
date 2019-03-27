package net.gegy1000.psf.server.block.fueler;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.gegy1000.psf.server.block.module.BlockFuelTank;
import net.gegy1000.psf.server.init.PSFFluids;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
public class TileFuelLoader extends TileEntity {
    private final IItemHandler inventory = new ItemStackHandler(NonNullList.withSize(2, ItemStack.EMPTY)) {
        @Override
        protected void onContentsChanged(int slot) {
            ItemStack stack = this.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
                IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                if (handler == null) {
                    return;
                }
                this.stacks.set(slot, fillFromItem(handler));

                IBlockState state = world.getBlockState(pos);
                world.notifyBlockUpdate(pos, state, state, 0);
            }

            markDirty();
        }
    };

    public Map<Fluid, FuelAmount> collectFuelAmounts() {
        IFluidHandler fluidHandler = getFluidHandler();
        Map<Fluid, FuelAmount> amounts = new HashMap<>();
        if (fluidHandler != null) {
            IFluidTankProperties[] tankProperties = fluidHandler.getTankProperties();
            for (IFluidTankProperties tank : tankProperties) {
                FluidStack contents = tank.getContents();
                if (contents != null) {
                    FuelAmount amount = amounts.computeIfAbsent(contents.getFluid(), fluid -> new FuelAmount());
                    amount.amount += contents.amount;
                    amount.capacity += tank.getCapacity();
                }
            }
        }
        return amounts;
    }

    private ItemStack fillFromItem(IFluidHandlerItem handler) {
        IFluidHandler fluidHandler = getFluidHandler();

        if (fluidHandler != null) {
            IFluidTankProperties[] tankProperties = handler.getTankProperties();
            for (IFluidTankProperties properties : tankProperties) {
                FluidStack contents = properties.getContents();
                if (contents != null) {
                    int originalAmount = contents.amount;
                    int filled = fluidHandler.fill(handler.drain(contents, true), true);
                    if (filled < originalAmount) {
                        handler.fill(new FluidStack(contents.getFluid(), originalAmount - filled), true);
                    }
                }
            }
        }

        return handler.getContainer();
    }

    private IFluidHandler getFluidHandler() {
        @Nullable World world = this.world;
        @Nullable BlockPos pos = this.pos;
        if (world == null || pos == null) return null;
        IBlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockFuelTank)) return null;
        EnumFacing facing = world.getBlockState(pos).getValue(BlockHorizontal.FACING);
        TileEntity entity = world.getTileEntity(pos.offset(facing));
        if (entity == null) return null;
        IFluidHandler handler = entity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite());
        return handler != null && isFuelTank(handler) ? handler : null;
    }

    private boolean isFuelTank(IFluidHandler handler) {
        IFluidTankProperties[] tankProperties = handler.getTankProperties();
        for (IFluidTankProperties properties : tankProperties) {
            FluidStack contents = properties.getContents();
            if (isAcceptableFluid(contents)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAcceptableFluid(@Nullable FluidStack stack) {
        return stack != null && (stack.getFluid() == PSFFluids.kerosene() || stack.getFluid() == PSFFluids.liquidOxygen());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setTag("inventory", CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.writeNBT(inventory, null));
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.readNBT(inventory, null, compound.getTagList("inventory", Constants.NBT.TAG_COMPOUND));
    }

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        handleUpdateTag(pkt.getNbtCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        readFromNBT(tag);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (CapabilityItemHandler.ITEM_HANDLER_CAPABILITY == capability) {
            return true;
        }
        if (CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY == capability) {
            return getFluidHandler() != null;
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(getFluidHandler());
        }
        return super.getCapability(capability, facing);
    }

    @EqualsAndHashCode
    public static class FuelAmount {
        @Getter
        @Setter
        private int amount;
        @Getter
        @Setter
        private int capacity;
    }
}

package net.gegy1000.psf.server.block.module;

import javax.annotation.Nonnull;

import lombok.Getter;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.server.capability.CapabilityModule;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class TileDummyModule extends TileModule {
    
    @Getter
    @Nonnull
    private BlockPos master = BlockPos.ORIGIN.up();

    @Override
    public IModule getModule() {
        TileEntity te = getWorld() == null ? null : getWorld().getTileEntity(getMaster());
        return te == null ? null : te.getCapability(CapabilityModule.INSTANCE, null);
    }

    public void setMaster(@Nonnull BlockPos master) {
        if (master.equals(this.getPos())) {
            throw new IllegalArgumentException("Dummy cannot be its own master!");
        }
        this.master = master;
    }
    
    @Override
    public @Nonnull NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        BlockPos master = getMaster();
        compound.setLong("master_pos", master.toLong());
        return compound;
    }
    
    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        setMaster(BlockPos.fromLong(compound.getLong("master_pos")));
    }
}

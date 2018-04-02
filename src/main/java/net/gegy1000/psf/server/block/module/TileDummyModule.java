package net.gegy1000.psf.server.block.module;

import javax.annotation.Nonnull;

import lombok.Getter;
import lombok.Setter;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.server.capability.CapabilityModule;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class TileDummyModule extends TileModule {
    
    @Setter
    @Getter
    @Nonnull
    private BlockPos master = BlockPos.ORIGIN;

    @Override
    public IModule getModule() {
        TileEntity te = getWorld() == null ? null : getWorld().getTileEntity(master);
        return te == null ? null : te.getCapability(CapabilityModule.INSTANCE, null);
    }
    
    @Override
    public @Nonnull NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setLong("master_pos", master.toLong());
        return compound;
    }
    
    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        setMaster(BlockPos.fromLong(compound.getLong("master_pos")));
    }
}

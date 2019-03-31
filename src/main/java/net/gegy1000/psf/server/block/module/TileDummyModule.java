package net.gegy1000.psf.server.block.module;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import lombok.Getter;
import net.gegy1000.psf.api.module.IModule;
import net.gegy1000.psf.server.capability.CapabilityModule;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileDummyModule extends TileModule {
    
    @Getter
    @Nonnull
    private BlockPos masterOffset = BlockPos.ORIGIN.up();

    @Override
    public IModule getModule() {
        @Nullable World world = this.world;
        if (world == null) return null;
        TileEntity te = world.getTileEntity(getMaster());
        if (te == null) return null;
        return te.getCapability(CapabilityModule.INSTANCE, null);
    }
    
    @Nonnull
    public BlockPos getMaster() {
        return getPos().add(getMasterOffset());
    }

    public void setMasterOffset(@Nonnull BlockPos offset) {
        if (offset.equals(BlockPos.ORIGIN)) {
            throw new IllegalArgumentException("Dummy cannot be its own master!");
        }
        this.masterOffset = offset;
    }
    
    public void setMaster(@Nonnull BlockPos pos) {
        setMasterOffset(pos.subtract(getPos()));
    }
    
    @Override
    public @Nonnull NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setLong("master_pos", getMasterOffset().toLong());
        return compound;
    }
    
    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        setMasterOffset(BlockPos.fromLong(compound.getLong("master_pos")));
    }
}

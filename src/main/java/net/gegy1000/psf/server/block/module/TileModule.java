package net.gegy1000.psf.server.block.module;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.server.capability.CapabilityModule;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

@NoArgsConstructor
@AllArgsConstructor
public class TileModule extends TileEntity {
    
    private IModule module;
    
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityModule.INSTANCE || super.hasCapability(capability, facing);
    }
    
    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityModule.INSTANCE) {
            return CapabilityModule.INSTANCE.cast(module);
        }
        return super.getCapability(capability, facing);
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        // TODO Auto-generated method stub
        return super.writeToNBT(compound);
    }
}

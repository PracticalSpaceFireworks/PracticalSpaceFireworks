package net.gegy1000.psf.server.modules;

import net.gegy1000.psf.api.IModule;
import net.minecraft.nbt.NBTTagCompound;

public abstract class EmptyModule implements IModule {
    
    @Override
    public NBTTagCompound serializeNBT() {
        return null;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {}
}

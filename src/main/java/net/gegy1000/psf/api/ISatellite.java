package net.gegy1000.psf.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.UUID;

public interface ISatellite {

    UUID getId();

    IController getController();
    
    Collection<IModule> getModules();
    
    BlockPos getPosition();

    NBTTagCompound serialize(NBTTagCompound compound);
}

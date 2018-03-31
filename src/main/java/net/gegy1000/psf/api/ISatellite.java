package net.gegy1000.psf.api;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public interface ISatellite {

    UUID getId();

    IController getController();
    
    Collection<IModule> getModules();
    
    BlockPos getPosition();
    
    Map<BlockPos, IBlockState> getComponents();
    
    // Other stuff about position/speed/etc ?

    NBTTagCompound serialize(NBTTagCompound compound);
}

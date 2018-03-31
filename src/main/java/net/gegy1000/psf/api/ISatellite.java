package net.gegy1000.psf.api;

import net.gegy1000.psf.server.entity.spacecraft.SpacecraftBlockAccess;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.UUID;

public interface ISatellite {

    UUID getId();

    IController getController();

    Collection<IModule> getModules();

    BlockPos getPosition();

    SpacecraftBlockAccess buildBlockAccess(BlockPos origin, World world);

    void requestModules();

    default NBTTagCompound serialize(NBTTagCompound compound) {
        return compound;
    }
}

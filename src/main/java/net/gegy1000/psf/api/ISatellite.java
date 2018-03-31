package net.gegy1000.psf.api;

import java.util.Collection;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.gegy1000.psf.server.entity.spacecraft.SpacecraftBlockAccess;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

@ParametersAreNonnullByDefault
public interface ISatellite extends INBTSerializable<NBTTagCompound> {

    default String getName() {
        return "Craft #" + hashCode();
    }
    
    default void setName(String name) {}
    
    UUID getId();

    IController getController();

    Collection<IModule> getModules();

    BlockPos getPosition();

    SpacecraftBlockAccess buildBlockAccess(BlockPos origin, World world);

    void requestModules();

    @Override
    default NBTTagCompound serializeNBT() {
        return new NBTTagCompound();
    }
    
    @Override
    default void deserializeNBT(@Nullable NBTTagCompound tag) {}
}

package net.gegy1000.psf.api;

import net.gegy1000.psf.server.block.remote.IListedSpacecraft;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftBlockAccess;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ParametersAreNonnullByDefault
public interface ISatellite extends IUnique, INBTSerializable<NBTTagCompound> {

    default String getName() {
        return "Unnamed Craft #" + hashCode() % 1000;
    }
    
    default void setName(String name) {}

    IController getController();

    Collection<IModule> getModules();

    default boolean tryExtractEnergy(int amount) {
        int extractedAmount = 0;
        for (IEnergyStorage storage : this.getModuleCaps(CapabilityEnergy.ENERGY)) {
            if (storage.canExtract()) {
                int extracted = storage.extractEnergy(amount - extractedAmount, false);
                extractedAmount += extracted;
                if (extractedAmount == amount) {
                    return true;
                }
            }
        }
        return false;
    }

    default <T> Collection<T> getModuleCaps(Capability<T> capability) {
        List<T> caps = new ArrayList<>();
        for (IModule module : this.getModules()) {
            if (module.hasCapability(capability, null)) {
                caps.add(module.getCapability(capability, null));
            }
        }
        return caps;
    }

    BlockPos getPosition();

    SpacecraftBlockAccess buildBlockAccess(World world);

    IListedSpacecraft toListedCraft();

    World getWorld();

    @Override
    default NBTTagCompound serializeNBT() {
        return new NBTTagCompound();
    }

    @Override
    default void deserializeNBT(@Nullable NBTTagCompound tag) {}

    default void tickSatellite(int ticksExisted) {
        for (IModule module : getModules()) {
            if (ticksExisted % module.getTickInterval() == 0) {
                module.onSatelliteTick(this);
            }
        }
    }
}

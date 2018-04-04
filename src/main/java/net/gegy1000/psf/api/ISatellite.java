package net.gegy1000.psf.api;

import com.google.common.base.Functions;
import net.gegy1000.psf.server.block.remote.IListedSpacecraft;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftBlockAccess;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public interface ISatellite extends IUnique, IListedSpacecraft, INBTSerializable<NBTTagCompound> {

    @Override
    default String getName() {
        return "Unnamed Craft #" + hashCode() % 1000;
    }
    
    @Override
    default void requestVisualData() {}
    
    IController getController();

    Collection<IModule> getModules();
    
    default Map<UUID, IModule> getIndexedModules() {
        return getModules().stream().collect(Collectors.toMap(IModule::getId, Functions.identity()));
    }

    default boolean tryExtractEnergy(int amount) {
        return extractEnergy(amount) == amount;
    }

    default int extractEnergy(int amount) {
        int extractedAmount = 0;
        for (IEnergyStorage storage : this.getModuleCaps(CapabilityEnergy.ENERGY)) {
            if (storage.canExtract()) {
                int extracted = storage.extractEnergy(amount - extractedAmount, false);
                extractedAmount += extracted;
                if (extractedAmount == amount) {
                    break;
                }
            }
        }
        return extractedAmount;
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

    SpacecraftBlockAccess buildBlockAccess(World world);

    IListedSpacecraft toListedCraft();

    World getWorld();

    @Override
    default NBTTagCompound serializeNBT() {
        return new NBTTagCompound();
    }

    @Override
    default void deserializeNBT(@Nullable NBTTagCompound tag) {}
   

    default void tickSatellite(long ticksExisted) {
        for (IModule module : getModules()) {
            if (isOrbiting() && ticksExisted % module.getTickInterval() == 0) {
                module.onSatelliteTick(this);
            }
            if (module.isDirty()) {
                sendModulePacket(module, module.getUpdateTag());
                module.dirty(false);
            }
        }
    }
    
    default void updateModuleClient(UUID id, NBTTagCompound data) {
        IModule module = getIndexedModules().get(id);
        if (module != null) {
            module.readUpdateTag(data);
        }
    }
    
    void sendModulePacket(IModule module, NBTTagCompound data);
    
    default Collection<EntityPlayerMP> getTrackingPlayers() {
        return Collections.emptyList();
    }

    default void track(EntityPlayerMP player) {}
    
    default void untrack(EntityPlayerMP player) {}
}

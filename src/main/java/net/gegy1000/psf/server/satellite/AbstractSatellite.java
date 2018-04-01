package net.gegy1000.psf.server.satellite;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractSatellite implements ISatellite {
    
    private Map<UUID, EntityPlayerMP> trackingPlayers = new HashMap<>();
    
    @Override
    public void sendModulePacket(IModule module, NBTTagCompound data) {
        for (EntityPlayerMP player : getTrackingPlayers()) {
            PSFNetworkHandler.network.sendTo(new PacketModule(module.getId(), data), player);
        }
    }
    
    @Override
    public @Nonnull Collection<EntityPlayerMP> getTrackingPlayers() {
        return trackingPlayers.values();
    }
    
    @Override
    public void track(EntityPlayerMP player) {
        this.trackingPlayers.put(player.getUniqueID(), player);
    }
    
    @Override
    public void untrack(EntityPlayerMP player) {
        this.trackingPlayers.remove(player.getUniqueID());
    }
}

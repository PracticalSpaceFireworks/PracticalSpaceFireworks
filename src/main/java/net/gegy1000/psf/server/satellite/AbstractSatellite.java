package net.gegy1000.psf.server.satellite;

import net.gegy1000.psf.api.module.IModule;
import net.gegy1000.psf.api.spacecraft.ISatellite;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ParametersAreNonnullByDefault
public abstract class AbstractSatellite implements ISatellite {
    
    private final Map<UUID, EntityPlayerMP> trackingPlayers = new HashMap<>();
    
    @Override
    public void sendModulePacket(@Nonnull IModule module, @Nonnull NBTTagCompound data) {
        for (EntityPlayerMP player : getTrackingPlayers()) {
            PSFNetworkHandler.network.sendTo(new PacketModule(module.getId(), data), player);
        }
    }
    
    @Override
    public @Nonnull Collection<EntityPlayerMP> getTrackingPlayers() {
        return trackingPlayers.values();
    }
    
    @Override
    public void track(@Nonnull EntityPlayerMP player) {
        this.trackingPlayers.put(player.getUniqueID(), player);
    }
    
    @Override
    public void untrack(@Nonnull EntityPlayerMP player) {
        this.trackingPlayers.remove(player.getUniqueID());
    }
}

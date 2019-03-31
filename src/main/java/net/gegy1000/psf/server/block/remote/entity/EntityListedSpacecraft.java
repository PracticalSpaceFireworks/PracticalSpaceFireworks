package net.gegy1000.psf.server.block.remote.entity;

import javax.annotation.Nonnull;

import java.util.UUID;

import net.gegy1000.psf.api.IListedSpacecraft;
import net.gegy1000.psf.server.block.remote.packet.PacketSetName;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.gegy1000.psf.server.entity.spacecraft.PacketLaunchCraft;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.util.math.BlockPos;

public class EntityListedSpacecraft implements IListedSpacecraft {
    private final EntitySpacecraft spacecraft;
    private final UUID uuid;

    public EntityListedSpacecraft(EntitySpacecraft spacecraft, UUID uuid) {
        this.spacecraft = spacecraft;
        this.uuid = uuid;
    }

    @Nonnull
    @Override
    public UUID getId() {
        return uuid;
    }

    @Nonnull
    @Override
    public String getName() {
        return this.spacecraft.getSatellite().getName();
    }

    @Override
    public void setName(@Nonnull String name) {
        PSFNetworkHandler.network.sendToServer(new PacketSetName(uuid, name));
        spacecraft.getSatellite().setName(name);
    }

    @Nonnull
    @Override
    public BlockPos getPosition() {
        return this.spacecraft.getPosition();
    }

    @Override
    public boolean canLaunch() {
        return this.spacecraft.getState().getType() == EntitySpacecraft.StateType.STATIC;
    }

    @Override
    public void launch() {
        PSFNetworkHandler.network.sendToServer(new PacketLaunchCraft(spacecraft.getEntityId()));
    }
    
    @Override
    public boolean isDestroyed() {
        return spacecraft.isDead;
    }
}

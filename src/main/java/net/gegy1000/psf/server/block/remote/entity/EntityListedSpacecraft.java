package net.gegy1000.psf.server.block.remote.entity;

import net.gegy1000.psf.api.spacecraft.IListedSpacecraft;
import net.gegy1000.psf.server.block.remote.packet.PacketSetName;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.gegy1000.psf.server.entity.spacecraft.PacketLaunchCraft;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.UUID;

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
    public Optional<LaunchHandle> getLaunchHandle() {
        if (this.spacecraft.getState().getType() == EntitySpacecraft.StateType.STATIC) {
            return Optional.of(() -> {
                PSFNetworkHandler.network.sendToServer(new PacketLaunchCraft(spacecraft.getEntityId()));
            });
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean isDestroyed() {
        return spacecraft.isDead;
    }
}

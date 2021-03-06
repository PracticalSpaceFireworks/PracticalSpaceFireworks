package net.gegy1000.psf.server.block.remote.orbiting;

import javax.annotation.Nonnull;

import java.util.UUID;

import net.gegy1000.psf.api.spacecraft.IListedSpacecraft;
import net.gegy1000.psf.server.block.remote.packet.PacketSetName;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.util.math.BlockPos;

public class OrbitingListedSpacecraft implements IListedSpacecraft  {
    private String name;
    private final BlockPos position;
    private final UUID uuid;

    public OrbitingListedSpacecraft(String name, BlockPos position, UUID uuid) {
        this.name = name;
        this.position = position;
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
        return this.name;
    }

    @Override
    public void setName(@Nonnull String name) {
        PSFNetworkHandler.network.sendToServer(new PacketSetName(this.uuid, name));
        this.name = name;
    }

    @Nonnull
    @Override
    public BlockPos getPosition() {
        return this.position;
    }

    @Override
    public boolean isOrbiting() {
        return true;
    }
    
    @Override
    public boolean isDestroyed() {
        return false;
    }
}

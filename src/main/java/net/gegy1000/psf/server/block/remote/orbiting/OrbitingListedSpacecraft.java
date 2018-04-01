package net.gegy1000.psf.server.block.remote.orbiting;

import net.gegy1000.psf.server.block.remote.IListedSpacecraft;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.UUID;

public class OrbitingListedSpacecraft implements IListedSpacecraft  {
    private final String name;
    private final BlockPos position;
    private final UUID uuid;

    public OrbitingListedSpacecraft(String name, BlockPos position, UUID uuid) {
        this.name = name;
        this.position = position;
        this.uuid = uuid;
    }

    @Nonnull
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(@Nonnull String name) {
        PSFNetworkHandler.network.sendToServer(new PacketSetNameOrbiting(this.uuid, name));
    }

    @Nonnull
    @Override
    public BlockPos getPosition() {
        return this.position;
    }

    @Override
    public void requestVisualData() {
        PSFNetworkHandler.network.sendToServer(new PacketRequestVisualOrbiting(this.uuid));
    }
    
    @Override
    public boolean isOrbiting() {
        return true;
    }
}

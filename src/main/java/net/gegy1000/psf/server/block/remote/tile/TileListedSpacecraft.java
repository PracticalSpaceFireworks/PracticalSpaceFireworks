package net.gegy1000.psf.server.block.remote.tile;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.block.remote.IListedSpacecraft;
import net.gegy1000.psf.server.block.remote.packet.PacketRequestVisual;
import net.gegy1000.psf.server.block.remote.packet.PacketSetName;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.util.math.BlockPos;

@ParametersAreNonnullByDefault
public class TileListedSpacecraft implements IListedSpacecraft {
    private final ISatellite satellite;

    public TileListedSpacecraft(ISatellite satellite) {
        this.satellite = satellite;
    }
    
    @Override
    public UUID getId() {
        return this.satellite.getId();
    }

    @Nonnull
    @Override
    public String getName() {
        return this.satellite.getName();
    }

    @Override
    public void setName(@Nonnull String name) {
        PSFNetworkHandler.network.sendToServer(new PacketSetName(satellite.getId(), name));
        satellite.setName(name);
    }

    @Nonnull
    @Override
    public BlockPos getPosition() {
        return satellite.getPosition();
    }

    @Override
    public void requestVisualData() {
        PSFNetworkHandler.network.sendToServer(new PacketRequestVisual(satellite.getId()));
    }
}

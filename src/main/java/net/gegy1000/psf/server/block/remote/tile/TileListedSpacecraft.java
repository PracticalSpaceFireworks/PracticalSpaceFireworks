package net.gegy1000.psf.server.block.remote.tile;

import net.gegy1000.psf.server.block.remote.IListedSpacecraft;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.gegy1000.psf.server.satellite.TileBoundSatellite;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TileListedSpacecraft implements IListedSpacecraft {
    private final TileBoundSatellite satellite;

    public TileListedSpacecraft(TileBoundSatellite satellite) {
        this.satellite = satellite;
    }

    @Nonnull
    @Override
    public String getName() {
        return this.satellite.getName();
    }

    @Override
    public void setName(@Nonnull String name) {
        PSFNetworkHandler.network.sendToServer(new PacketSetNameTile(satellite.getPosition(), name));
        satellite.setName(name);
    }

    @Nonnull
    @Override
    public BlockPos getPosition() {
        return satellite.getPosition();
    }

    @Override
    public void requestVisualData() {
        PSFNetworkHandler.network.sendToServer(new PacketRequestVisualTile(getPosition()));
    }
}

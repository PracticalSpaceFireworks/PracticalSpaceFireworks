package net.gegy1000.psf.server.compat.waila;

import lombok.val;
import mcp.MethodsReturnNonnullByDefault;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;
import net.gegy1000.psf.server.block.module.BlockFuelValve;
import net.gegy1000.psf.server.compat.waila.provider.FuelValveDataProvider;
import net.gegy1000.psf.server.compat.waila.provider.SpacecraftDataProvider;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;

import javax.annotation.ParametersAreNonnullByDefault;

@WailaPlugin
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class PSFWailaPlugin implements IWailaPlugin {
    @Override
    public void register(IWailaRegistrar registrar) {
        registrar.registerBodyProvider(new FuelValveDataProvider(), BlockFuelValve.class);
        val spacecraftDataProvider = new SpacecraftDataProvider();
        registrar.registerHeadProvider(spacecraftDataProvider, EntitySpacecraft.class);
        registrar.registerBodyProvider(spacecraftDataProvider, EntitySpacecraft.class);
        registrar.registerTailProvider(spacecraftDataProvider, EntitySpacecraft.class);
    }
}

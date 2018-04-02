package net.gegy1000.psf.server.block.module;

import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.server.capability.CapabilityModule;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class TileDummyModule extends TileModule {
    
    private BlockPos master;
    
    public TileDummyModule(BlockPos master) {
        this.master = master;
    }

    @Override
    public IModule getModule() {
        TileEntity te = getWorld().getTileEntity(master);
        return te == null ? null : te.getCapability(CapabilityModule.INSTANCE, null);
    }
}

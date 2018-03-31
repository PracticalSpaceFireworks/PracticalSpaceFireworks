package net.gegy1000.psf.server.api;

import net.minecraft.tileentity.TileEntity;

public interface RegisterTileEntity {
    Class<? extends TileEntity> getEntityClass();
}

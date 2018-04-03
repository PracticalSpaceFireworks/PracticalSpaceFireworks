package net.gegy1000.psf.server.util;

import net.gegy1000.psf.server.block.fueler.ContainerFuelLoader;
import net.gegy1000.psf.server.block.fueler.GuiFuelLoader;
import net.gegy1000.psf.server.block.fueler.TileFuelLoader;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class PSFGuiHandler implements IGuiHandler {
    public static final int ID_FUEL_LOADER = 0;

    @Override
    @Nullable
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity entity = world.getTileEntity(new BlockPos(x, y, z));
        switch (id) {
            case ID_FUEL_LOADER:
                if (entity instanceof TileFuelLoader) {
                    return new ContainerFuelLoader((TileFuelLoader) entity, player.inventory);
                }
                break;
        }
        return null;
    }

    @Override
    @Nullable
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity entity = world.getTileEntity(new BlockPos(x, y, z));
        switch (id) {
            case ID_FUEL_LOADER:
                if (entity instanceof TileFuelLoader) {
                    return new GuiFuelLoader(new ContainerFuelLoader((TileFuelLoader) entity, player.inventory));
                }
                break;
        }
        return null;
    }
}

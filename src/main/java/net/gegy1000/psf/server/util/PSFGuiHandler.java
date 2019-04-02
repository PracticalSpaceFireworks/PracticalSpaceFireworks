package net.gegy1000.psf.server.util;

import net.gegy1000.psf.api.module.IModule;
import net.gegy1000.psf.server.block.module.TileModule;
import net.gegy1000.psf.server.block.production.ContainerAirCompressor;
import net.gegy1000.psf.server.block.production.ContainerKeroseneExtractor;
import net.gegy1000.psf.server.block.production.GuiAirCompressor;
import net.gegy1000.psf.server.block.production.GuiKeroseneExtractor;
import net.gegy1000.psf.server.block.production.TileAirCompressor;
import net.gegy1000.psf.server.block.production.TileKeroseneExtractor;
import net.gegy1000.psf.server.block.valve.ContainerFuelValve;
import net.gegy1000.psf.server.block.valve.GuiFuelValve;
import net.gegy1000.psf.server.modules.ModuleFuelValve;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class PSFGuiHandler implements IGuiHandler {
    public static final int ID_FUEL_VALVE = 0;
    public static final int ID_AIR_COMPRESSOR = 1;
    public static final int ID_KEROSENE_EXTRACTOR = 2;

    @Override
    @Nullable
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity entity = world.getTileEntity(new BlockPos(x, y, z));
        IModule module = TileModule.getModule(entity);
        switch (id) {
            case ID_FUEL_VALVE:
                if (module instanceof ModuleFuelValve && module.getOwner() != null) {
                    return new ContainerFuelValve(world, (ModuleFuelValve) module, player.inventory);
                }
                break;
            case ID_AIR_COMPRESSOR:
                if (entity instanceof TileAirCompressor) {
                    return new ContainerAirCompressor((TileAirCompressor) entity, player.inventory);
                }
                break;
            case ID_KEROSENE_EXTRACTOR:
                if (entity instanceof TileKeroseneExtractor) {
                    return new ContainerKeroseneExtractor((TileKeroseneExtractor) entity, player.inventory);
                }
                break;
        }
        return null;
    }

    @Override
    @Nullable
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity entity = world.getTileEntity(new BlockPos(x, y, z));
        IModule module = TileModule.getModule(entity);
        switch (id) {
            case ID_FUEL_VALVE:
                if (module instanceof ModuleFuelValve) {
                    return new GuiFuelValve(new ContainerFuelValve(world, (ModuleFuelValve) module, player.inventory));
                }
                break;
            case ID_AIR_COMPRESSOR:
                if (entity instanceof TileAirCompressor) {
                    return new GuiAirCompressor(new ContainerAirCompressor((TileAirCompressor) entity, player.inventory));
                }
                break;
            case ID_KEROSENE_EXTRACTOR:
                if (entity instanceof TileKeroseneExtractor) {
                    return new GuiKeroseneExtractor(new ContainerKeroseneExtractor((TileKeroseneExtractor) entity, player.inventory));
                }
                break;
        }
        return null;
    }
}

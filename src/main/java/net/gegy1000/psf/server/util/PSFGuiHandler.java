package net.gegy1000.psf.server.util;

import javax.annotation.Nullable;

import net.gegy1000.psf.server.block.remote.ContainerControlSystem;
import net.gegy1000.psf.server.block.remote.GuiControlSystem;
import net.gegy1000.psf.server.block.remote.TileRemoteControlSystem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;


public class PSFGuiHandler implements IGuiHandler {
    
    public static final int ID_CONTROL_SYSTEM = 0;

    @Override
    @Nullable
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z)); 
        switch(ID) {
            case ID_CONTROL_SYSTEM:
                if (te instanceof TileRemoteControlSystem) {
                    return new ContainerControlSystem((TileRemoteControlSystem) te, player.inventory);
                }
                break;
            default:
                break;
        }
        return null;
    }

    @Override
    @Nullable
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z)); 
        switch(ID) {
            case ID_CONTROL_SYSTEM:
                if (te instanceof TileRemoteControlSystem) {
                    return new GuiControlSystem(new ContainerControlSystem((TileRemoteControlSystem) te, player.inventory));
                }
                break;
            default:
                break;
        }
        return null;
    }

}

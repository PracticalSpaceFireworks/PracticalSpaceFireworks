package net.gegy1000.psf.server.util;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;

public abstract class GuiDummyContainer extends GuiContainer {

    public GuiDummyContainer(TileEntity te) {
        super(new Container() {

            @Override
            public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
                if (te.getWorld().getTileEntity(te.getPos()) != te) {
                    return false;
                } else {
                    return playerIn.getDistanceSq(te.getPos().getX() + 0.5, te.getPos().getY() + 0.5, te.getPos().getZ() + 0.5) <= 64;
                }
            }
        });
    }
}

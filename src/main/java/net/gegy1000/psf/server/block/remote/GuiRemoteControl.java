package net.gegy1000.psf.server.block.remote;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import lombok.Getter;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.client.IVisualReceiver;
import net.gegy1000.psf.server.block.remote.packet.PacketTrackCraft;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public abstract class GuiRemoteControl extends GuiContainer implements IVisualReceiver {
    
    @Nonnull
    protected static final ResourceLocation TEXTURE_LOC = new ResourceLocation(PracticalSpaceFireworks.MODID, "textures/gui/control_system.png");

    @Getter
    private final TileRemoteControlSystem te;

    protected GuiRemoteControl(TileRemoteControlSystem te) {
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
        this.te = te;
        
        xSize = 256;
        ySize = 201;
    }
    
    @Override
    public void initGui() {
        super.initGui();
        
        IListedSpacecraft craft = getCraft();
        if (craft != null) {
            PSFNetworkHandler.network.sendToServer(new PacketTrackCraft(craft.getId(), true));
        }
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1, 1, 1, 1);
        drawDefaultBackground();
        GlStateManager.color(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(TEXTURE_LOC);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);        
    }
    
    @Override
    public void setVisual(IVisual visual) {}

    @Override
    public void updateModule(UUID id, NBTTagCompound tag) {}
    
    @Override
    public void updateCraft(@Nonnull IListedSpacecraft craft) {
        te.provideSingleCraft(craft);
    }
    
    protected @Nullable IListedSpacecraft getCraft() {
        return null;
    }

    protected void untrack() {
        IListedSpacecraft craft = getCraft();
        if (craft != null) {
            PSFNetworkHandler.network.sendToServer(new PacketTrackCraft(craft.getId(), false));
        }
    }
    
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        untrack();
    }
}

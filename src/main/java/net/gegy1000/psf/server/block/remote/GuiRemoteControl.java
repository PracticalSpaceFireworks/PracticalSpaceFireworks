package net.gegy1000.psf.server.block.remote;

import lombok.Getter;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.client.IVisualData;
import net.gegy1000.psf.api.client.IVisualReceiver;
import net.gegy1000.psf.api.spacecraft.IListedSpacecraft;
import net.gegy1000.psf.server.block.remote.packet.PacketTrackCraft;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.UUID;

public abstract class GuiRemoteControl extends GuiContainer implements IVisualReceiver {
    
    @Nonnull
    protected static final ResourceLocation TEXTURE_LOC = new ResourceLocation(PracticalSpaceFireworks.MODID, "textures/gui/control_system.png");
    
    @Getter
    @Nullable
    private final GuiScreen parent;

    @Getter
    private final TileRemoteControlSystem te;

    protected GuiRemoteControl(@Nullable GuiScreen parent, TileRemoteControlSystem te) {
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
        this.parent = parent;
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
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        GuiScreen parent = getParent();
        if (parent != null && mouseButton == 1) {
            Minecraft.getMinecraft().displayGuiScreen(parent);
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
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
    public void setVisual(@Nonnull IVisualData visual) {}

    @Override
    public void updateModule(@Nonnull UUID id, @Nonnull NBTTagCompound tag) {}
    
    @Override
    public void updateCraft(@Nonnull IListedSpacecraft craft) {
        te.provideSingleCraft(craft);
    }
    
    @Override
    public void removeCraft(UUID id) {
        te.removeCraft(id);
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

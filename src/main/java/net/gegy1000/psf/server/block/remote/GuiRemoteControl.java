package net.gegy1000.psf.server.block.remote;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.io.IOException;
import java.util.UUID;

import lombok.Getter;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.client.IVisualReceiver;
import net.gegy1000.psf.server.block.remote.packet.PacketTrackCraft;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.gegy1000.psf.server.util.GuiDummyContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public abstract class GuiRemoteControl extends GuiDummyContainer implements IVisualReceiver {
    
    @Nonnull
    protected static final ResourceLocation TEXTURE_LOC = new ResourceLocation(PracticalSpaceFireworks.MODID, "textures/gui/control_system.png");
    
    @Getter
    @Nullable
    private final GuiScreen parent;

    @Getter
    private final TileCraftList te;

    protected GuiRemoteControl(@Nullable GuiScreen parent, TileCraftList te) {
        super(te);
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
    public void setVisual(@Nonnull IVisual visual) {}

    @Override
    public void updateModule(@Nonnull UUID id, @Nonnull NBTTagCompound tag) {}
    
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

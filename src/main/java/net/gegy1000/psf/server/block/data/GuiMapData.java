package net.gegy1000.psf.server.block.data;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Rectangle;

import javax.annotation.Nonnull;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import net.gegy1000.psf.api.data.ITerrainScan;
import net.gegy1000.psf.client.GlUtil;
import net.gegy1000.psf.client.IVisualReceiver;
import net.gegy1000.psf.server.block.remote.GuiCraftDetails.SyncedData;
import net.gegy1000.psf.server.block.remote.packet.PacketRequestVisual;
import net.gegy1000.psf.server.block.remote.IListedSpacecraft;
import net.gegy1000.psf.server.block.remote.MapRenderer;
import net.gegy1000.psf.server.block.remote.TileCraftList;
import net.gegy1000.psf.server.capability.CapabilityModuleData;
import net.gegy1000.psf.server.modules.ModuleTerrainScanner;
import net.gegy1000.psf.server.modules.data.EmptyTerrainScan;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.gegy1000.psf.server.util.GuiDummyContainer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.nbt.NBTTagCompound;

public class GuiMapData extends GuiDummyContainer implements IVisualReceiver {
    
    private final TileCraftList te;
    
    private SyncedData synced;
    private MapRenderer mapRenderer;
    
    private final Rectangle panel;
    
    public GuiMapData(TileCraftList te) {
        super(te);
        this.te = te;
        
        xSize = 256;
        ySize = 201;

        panel = new Rectangle(10, 10, xSize - 10, ySize - 10);
    }
    
    @Override
    public void initGui() {
        super.initGui();
        
        IListedSpacecraft craft = te.getCrafts().stream().filter(s -> s.isOrbiting()).findFirst().get();
        if (craft != null) {
            PSFNetworkHandler.network.sendToServer(new PacketRequestVisual(craft.getId()));
        }
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        if (synced != null) {
            renderMap(synced);
        }
    }

    private void renderMap(SyncedData synced) {
        Optional<ITerrainScan> terrainScan = synced.getTerrainScannerModules().stream()
                .map(module -> module.getCapability(CapabilityModuleData.TERRAIN_SCAN, null))
                .filter(Objects::nonNull)
                .findFirst();

        ITerrainScan buildScan = terrainScan.orElseGet(() -> new EmptyTerrainScan(ModuleTerrainScanner.SCAN_RANGE));
        if (mapRenderer == null || mapRenderer.shouldUpdate(buildScan)) {
            if (mapRenderer != null) {
                mapRenderer.delete();
            }
            mapRenderer = new MapRenderer(buildScan);
        }

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableDepth();

        if (GlUtil.SCISSOR_AVAILABLE) {
            ScaledResolution sr = new ScaledResolution(mc);
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor((guiLeft + panel.getX()) * sr.getScaleFactor(), mc.displayHeight - ((guiTop + panel.getY() + panel.getHeight()) * sr.getScaleFactor()),
                    panel.getWidth() * sr.getScaleFactor(), panel.getHeight() * sr.getScaleFactor());
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.translate(guiLeft + (xSize / 2), guiTop + ySize / 2, 500);

        GlStateManager.rotate(-45.0F, 1.0F, 0.0F, 0.0F);
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.rotate(mc.player.ticksExisted + mc.getRenderPartialTicks(), 0, 1, 0);
        GlStateManager.scale(-1.8, -1.8, -1.8);

        GlStateManager.translate(-8.0, 0.0, -8.0);

        mapRenderer.performUploads();
        mapRenderer.render();

        if (GlUtil.SCISSOR_AVAILABLE) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }

        GlStateManager.disableDepth();
        GlStateManager.disableRescaleNormal();
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.popMatrix();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (mapRenderer != null) {
            mapRenderer.delete();
        }
    }

    @Override
    public void setVisual(@Nonnull IVisual visual) {
        this.synced = new SyncedData(visual);
    }

    @Override
    public void updateCraft(@Nonnull IListedSpacecraft craft) {
        te.provideSingleCraft(craft);
    }

    @Override
    public void updateModule(@Nonnull UUID id, @Nonnull NBTTagCompound tag) {
        if (synced != null) {
            synced.getModules().stream().filter(m -> m.getId().equals(id)).findFirst().ifPresent(m -> m.readUpdateTag(tag));
        }
    }
}

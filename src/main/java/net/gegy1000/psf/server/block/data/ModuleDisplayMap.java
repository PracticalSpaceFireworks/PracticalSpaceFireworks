package net.gegy1000.psf.server.block.data;

import java.util.ArrayList;

import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;
import net.gegy1000.psf.api.data.IModuleDataDisplay;
import net.gegy1000.psf.server.block.remote.MapRenderer;
import net.gegy1000.psf.server.modules.data.CompositeTerrainScan;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;

@NoArgsConstructor
public class ModuleDisplayMap implements IModuleDataDisplay {
    
    @Delegate
    private CompositeTerrainScan scan = new CompositeTerrainScan(new ArrayList<>());
    
    private MapRenderer mapRenderer;
    
    public ModuleDisplayMap(CompositeTerrainScan scan) {
        this.scan = scan;
    }
    
    @Override
    public void draw(int x, int y, int width, int height, float partialTicks) {
        if (mapRenderer == null || mapRenderer.shouldUpdate(scan)) {
            if (mapRenderer != null) {
                mapRenderer.delete();
            }
            mapRenderer = new MapRenderer(scan);
        }

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableDepth();
        
        Minecraft mc = Minecraft.getMinecraft();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.translate(x + (width / 2), y + (height / 2), 500);

        GlStateManager.rotate(-45.0F, 1.0F, 0.0F, 0.0F);
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.rotate(mc.player.ticksExisted + mc.getRenderPartialTicks(), 0, 1, 0);
        GlStateManager.scale(-1.8, -1.8, -1.8);

        GlStateManager.translate(-8.0, 0.0, -8.0);

        mapRenderer.performUploads();
        mapRenderer.render();

        GlStateManager.disableDepth();
        GlStateManager.disableRescaleNormal();
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.popMatrix();
    
    }
}

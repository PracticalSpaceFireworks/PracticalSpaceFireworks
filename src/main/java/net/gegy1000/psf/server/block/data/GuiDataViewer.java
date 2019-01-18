package net.gegy1000.psf.server.block.data;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Rectangle;

import net.gegy1000.psf.api.data.IModuleDataDisplay;
import net.gegy1000.psf.api.data.IModuleDataDisplayFactory;
import net.gegy1000.psf.client.GlUtil;
import net.gegy1000.psf.client.IDataDisplay;
import net.gegy1000.psf.server.block.remote.TileCraftList;
import net.gegy1000.psf.server.modules.data.ModuleDisplays;
import net.gegy1000.psf.server.util.GuiDummyContainer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;

public class GuiDataViewer extends GuiDummyContainer implements IDataDisplay {
    
    private final TileCraftList te;
    
    private final Rectangle panel;
    
    private final IModuleDataDisplayFactory displayFactory = ModuleDisplays.get().getValue(new ResourceLocation("psf:map"));
    
    private IModuleDataDisplay display;
    
    public GuiDataViewer(TileCraftList te) {
        super(te);
        this.te = te;
        
        xSize = 256;
        ySize = 201;

        panel = new Rectangle(10, 10, xSize - 10, ySize - 10);
    }
    
    @Override
    public void initGui() {
        super.initGui();
        requestDisplay(displayFactory);
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        if (display == null) return;
        
        if (GlUtil.SCISSOR_AVAILABLE) {
            ScaledResolution sr = new ScaledResolution(mc);
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor((guiLeft + panel.getX()) * sr.getScaleFactor(), mc.displayHeight - ((guiTop + panel.getY() + panel.getHeight()) * sr.getScaleFactor()),
                    panel.getWidth() * sr.getScaleFactor(), panel.getHeight() * sr.getScaleFactor());
        }
        
        display.draw(guiLeft + panel.getX(), guiTop + panel.getY(), panel.getWidth(), panel.getHeight(), partialTicks);

        if (GlUtil.SCISSOR_AVAILABLE) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    }

    @Override
    public void setDisplay(IModuleDataDisplay display) {
        this.display = display;
    }
}

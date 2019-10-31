package net.gegy1000.psf.server.block.production;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.client.gui.TankRenderer;
import net.gegy1000.psf.server.init.PSFBlocks;
import net.gegy1000.psf.server.init.PSFFluids;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class GuiAirCompressor extends GuiContainer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(PracticalSpaceFireworks.MODID, "textures/gui/air_compressor.png");

    private static final int TANK_WIDTH = 16;
    private static final int TANK_HEIGHT = 63;

    private final ContainerAirCompressor container;

    public GuiAirCompressor(ContainerAirCompressor container) {
        super(container);
        this.container = container;
    }
    
    private TankRenderer airTank, compressedAirTank;
    
    @Override
    public void initGui() {
        super.initGui();
        
        airTank = new TankRenderer(52, 14, TANK_WIDTH, TANK_HEIGHT, this.width, this.height);
        compressedAirTank = new TankRenderer(106, 14, TANK_WIDTH, TANK_HEIGHT, this.width, this.height);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        int originX = (width - xSize) / 2;
        int originY = (height - ySize) / 2;
        mouseX -= originX;
        mouseY -= originY;

        String title = I18n.format(PSFBlocks.AIR_COMPRESSOR.getTranslationKey() + ".name");
        fontRenderer.drawString(title, (xSize - fontRenderer.getStringWidth(title)) / 2, 4, 0x404040);

        GlStateManager.color(1, 1, 1, 1);
        airTank.drawTooltip(PSFFluids.FILTERED_AIR.getFluid(), container.getAirAmount(), TileAirCompressor.TANK_SIZE, mouseX, mouseY);
        compressedAirTank.drawTooltip(PSFFluids.COMPRESSED_AIR.getFluid(), container.getCompressedAirAmount(), TileAirCompressor.TANK_SIZE, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEXTURE);

        int originX = (width - xSize) / 2;
        int originY = (height - ySize) / 2;

        GlStateManager.color(1, 1, 1, 1);
        drawTexturedModalRect(originX, originY, 0, 0, xSize, ySize);
        
        if (container.isActive()) {
            drawTexturedModalRect(originX + 72, originY + 31, 176, 0, 33, 33);
        }
        
        airTank.draw(PSFFluids.FILTERED_AIR.getFluid(), container.getAirAmount(), TileAirCompressor.TANK_SIZE, originX, originY);
        compressedAirTank.draw(PSFFluids.COMPRESSED_AIR.getFluid(), container.getCompressedAirAmount(), TileAirCompressor.TANK_SIZE, originX, originY);
    }
}

package net.gegy1000.psf.server.block.production;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.client.gui.TankRenderer;
import net.gegy1000.psf.server.init.PSFBlocks;
import net.gegy1000.psf.server.init.PSFFluids;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class GuiAirSeparator extends GuiContainer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(PracticalSpaceFireworks.MODID, "textures/gui/air_separator.png");

    private static final int TANK_WIDTH = 16;
    private static final int TANK_HEIGHT = 63;

    private final ContainerAirSeparator container;

    public GuiAirSeparator(ContainerAirSeparator container) {
        super(container);
        this.container = container;
    }
    
    private TankRenderer compressedAirTank, liquidNitrogenTank, liquidOxygenTank;
    
    @Override
    public void initGui() {
        super.initGui();
        
        compressedAirTank = new TankRenderer(79, 14, TANK_WIDTH, TANK_HEIGHT, this.width, this.height);
        liquidNitrogenTank = new TankRenderer(34, 14, TANK_WIDTH, TANK_HEIGHT, this.width, this.height);
        liquidOxygenTank = new TankRenderer(124, 14, TANK_WIDTH, TANK_HEIGHT, this.width, this.height);
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

        String title = I18n.format(PSFBlocks.AIR_SEPARATOR.getTranslationKey() + ".name");
        fontRenderer.drawString(title, (xSize - fontRenderer.getStringWidth(title)) / 2, 4, 0x404040);

        GlStateManager.color(1, 1, 1, 1);
        compressedAirTank.drawTooltip(PSFFluids.compressedAir(), container.getCompressedAirAmount().getAmount(), container.getCompressedAirAmount().getCapacity(), mouseX, mouseY);
        liquidNitrogenTank.drawTooltip(PSFFluids.liquidNitrogen(), container.getLiquidNitrogenAmount().getAmount(), container.getLiquidNitrogenAmount().getCapacity(), mouseX, mouseY);
        liquidOxygenTank.drawTooltip(PSFFluids.liquidOxygen(), container.getLiquidOxygenAmount().getAmount(), container.getLiquidOxygenAmount().getCapacity(), mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEXTURE);

        int originX = (width - xSize) / 2;
        int originY = (height - ySize) / 2;

        GlStateManager.color(1, 1, 1, 1);
        drawTexturedModalRect(originX, originY, 0, 0, xSize, ySize);
        
        if (container.isActive()) {
            drawTexturedModalRect(originX + 65, originY + 29, 176, 0, 46, 30);
        }
        
        compressedAirTank.draw(PSFFluids.compressedAir(), container.getCompressedAirAmount().getAmount(), container.getCompressedAirAmount().getCapacity(), originX, originY);
        liquidNitrogenTank.draw(PSFFluids.liquidNitrogen(), container.getLiquidNitrogenAmount().getAmount(), container.getLiquidNitrogenAmount().getCapacity(), originX, originY);
        liquidOxygenTank.draw(PSFFluids.liquidOxygen(), container.getLiquidOxygenAmount().getAmount(), container.getLiquidOxygenAmount().getCapacity(), originX, originY);
    }
}

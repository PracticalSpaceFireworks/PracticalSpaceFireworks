package net.gegy1000.psf.server.block.production;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.client.gui.TankRenderer;
import net.gegy1000.psf.server.init.PSFBlocks;
import net.gegy1000.psf.server.init.PSFFluids;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class GuiKeroseneExtractor extends GuiContainer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(PracticalSpaceFireworks.MODID, "textures/gui/kerosene_extractor.png");

    private static final int TANK_WIDTH = 28;
    private static final int TANK_HEIGHT = 59;

    private final ContainerKeroseneExtractor container;

    public GuiKeroseneExtractor(ContainerKeroseneExtractor container) {
        super(container);
        this.container = container;
    }

    private TankRenderer keroseneTank;

    @Override
    public void initGui() {
        super.initGui();

        keroseneTank = new TankRenderer(116, 14, TANK_WIDTH, TANK_HEIGHT, this.width, this.height);
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

        String title = I18n.format(PSFBlocks.KEROSENE_EXTRACTOR.getTranslationKey() + ".name");
        fontRenderer.drawString(title, (xSize - fontRenderer.getStringWidth(title)) / 2, 5, 0x404040);

        GlStateManager.color(1, 1, 1, 1);
        keroseneTank.drawTooltip(PSFFluids.KEROSENE.getFluid(), container.getKeroseneAmount(), TileKeroseneExtractor.MAX_STORAGE, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEXTURE);

        int originX = (width - xSize) / 2;
        int originY = (height - ySize) / 2;

        GlStateManager.color(1, 1, 1, 1);
        drawTexturedModalRect(originX, originY, 0, 0, xSize, ySize);

        if (container.isActive()) {
            drawTexturedModalRect(originX + 50, originY + 56, 176, 0, 14, 14);
        }

        int time = container.getExtractionTime();
        int total = container.getExtractionAmount();
        int progressWidth = total != 0 ? time * 24 / total : 0;
        drawTexturedModalRect(originX + 82, originY + 37, 176, 14, progressWidth, 17);

        keroseneTank.draw(PSFFluids.KEROSENE.getFluid(), container.getKeroseneAmount(), TileKeroseneExtractor.MAX_STORAGE, originX, originY);
    }
}

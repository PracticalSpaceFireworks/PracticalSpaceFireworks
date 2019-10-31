package net.gegy1000.psf.server.block.valve;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.client.gui.TankRenderer;
import net.gegy1000.psf.server.init.PSFBlocks;
import net.gegy1000.psf.server.init.PSFFluids;
import net.gegy1000.psf.server.modules.FuelState;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class GuiFuelValve extends GuiContainer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(PracticalSpaceFireworks.MODID, "textures/gui/fuel_valve.png");

    private static final int TANK_WIDTH = 20;
    private static final int TANK_HEIGHT = 63;

    private final ContainerFuelValve container;

    public GuiFuelValve(ContainerFuelValve container) {
        super(container);
        this.container = container;
    }
    
    private TankRenderer keroseneTank, loxTank;
    
    @Override
    public void initGui() {
        super.initGui();
        
        keroseneTank = new TankRenderer(61, 13, TANK_WIDTH, TANK_HEIGHT, this.width, this.height);
        loxTank = new TankRenderer(91, 13, TANK_WIDTH, TANK_HEIGHT, this.width, this.height);
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

        String title = I18n.format(PSFBlocks.FUEL_VALVE.getTranslationKey() + ".name");
        fontRenderer.drawString(title, (xSize - fontRenderer.getStringWidth(title)) / 2, 4, 0x404040);

        GlStateManager.color(1, 1, 1, 1);
        FuelState keroseneState = container.getKeroseneState();
        FuelState liquidOxygenState = container.getLiquidOxygenState();

        keroseneTank.drawTooltip(PSFFluids.KEROSENE.getFluid(), keroseneState.getAmount(), keroseneState.getCapacity(), mouseX, mouseY);
        loxTank.drawTooltip(PSFFluids.LIQUID_OXYGEN.getFluid(), liquidOxygenState.getAmount(), liquidOxygenState.getCapacity(), mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEXTURE);

        int originX = (width - xSize) / 2;
        int originY = (height - ySize) / 2;

        GlStateManager.color(1, 1, 1, 1);
        drawTexturedModalRect(originX, originY, 0, 0, xSize, ySize);
        
        GlStateManager.color(1, 1, 1, 1);
        FuelState keroseneState = container.getKeroseneState();
        FuelState liquidOxygenState = container.getLiquidOxygenState();
        
        keroseneTank.draw(PSFFluids.KEROSENE.getFluid(), keroseneState.getAmount(), keroseneState.getCapacity(), originX, originY);
        loxTank.draw(PSFFluids.LIQUID_OXYGEN.getFluid(), liquidOxygenState.getAmount(), liquidOxygenState.getCapacity(), originX, originY);
    }
}

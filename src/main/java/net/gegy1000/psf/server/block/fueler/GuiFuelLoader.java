package net.gegy1000.psf.server.block.fueler;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.block.PSFBlockRegistry;
import net.gegy1000.psf.server.fluid.PSFFluidRegistry;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import java.util.ArrayList;
import java.util.List;

public class GuiFuelLoader extends GuiContainer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(PracticalSpaceFireworks.MODID, "textures/gui/fuel_loader.png");
    private static final boolean SCISSOR_AVAILABLE = GLContext.getCapabilities().OpenGL20;

    private static final int TANK_WIDTH = 20;
    private static final int TANK_HEIGHT = 63;

    private final ContainerFuelLoader container;

    public GuiFuelLoader(ContainerFuelLoader container) {
        super(container);
        this.container = container;
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

        String title = I18n.format(PSFBlockRegistry.fuelLoader.getUnlocalizedName() + ".name");
        fontRenderer.drawString(title, (xSize - fontRenderer.getStringWidth(title)) / 2, 4, 0x404040);

        GlStateManager.color(1, 1, 1, 1);
        TileFuelLoader.FuelAmount keroseneAmount = container.getKeroseneAmount();
        TileFuelLoader.FuelAmount liquidOxygenAmount = container.getLiquidOxygenAmount();

        drawTank(PSFFluidRegistry.KEROSENE, keroseneAmount.getAmount(), keroseneAmount.getCapacity(), 61, 13);
        drawTank(PSFFluidRegistry.LIQUID_OXYGEN, liquidOxygenAmount.getAmount(), liquidOxygenAmount.getCapacity(), 91, 13);

        drawTankTooltip(PSFFluidRegistry.KEROSENE, keroseneAmount.getAmount(), keroseneAmount.getCapacity(), 61, 13, mouseX, mouseY);
        drawTankTooltip(PSFFluidRegistry.LIQUID_OXYGEN, liquidOxygenAmount.getAmount(), liquidOxygenAmount.getCapacity(), 91, 13, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEXTURE);

        int originX = (width - xSize) / 2;
        int originY = (height - ySize) / 2;

        drawTexturedModalRect(originX, originY, 0, 0, xSize, ySize);
    }

    private void drawTank(Fluid fluid, int amount, int capacity, int x, int y) {
        GlStateManager.enableBlend();

        TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(fluid.getStill().toString());

        float scale = (float) amount / capacity;
        int height = (int) (scale * TANK_HEIGHT);
        drawSpriteTiled(sprite, x + 1, y + 1 + (TANK_HEIGHT - height), TANK_WIDTH, height);

        GlStateManager.disableBlend();
    }

    private void drawTankTooltip(Fluid fluid, int amount, int capacity, int x, int y, int mouseX, int mouseY) {
        if (mouseX >= x && mouseY >= y && mouseX <= x + TANK_WIDTH && mouseY <= y + TANK_HEIGHT) {
            List<String> lines = new ArrayList<>();
            lines.add(fluid.getRarity().rarityColor + I18n.format(fluid.getUnlocalizedName()));
            lines.add(TextFormatting.BLUE.toString() + amount + "/" + capacity + " mB");

            float percentage = capacity == 0 ? 0 : (float) amount / capacity * 100;
            lines.add(TextFormatting.GRAY.toString() + TextFormatting.ITALIC + String.format("%.1f%%", percentage));

            GuiUtils.drawHoveringText(lines, mouseX, mouseY, width, height, 100, fontRenderer);
        }
    }

    private void drawSpriteTiled(TextureAtlasSprite sprite, int x, int y, int width, int height) {
        if (SCISSOR_AVAILABLE) {
            ScaledResolution resolution = new ScaledResolution(mc);
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor((guiLeft + x) * resolution.getScaleFactor(), mc.displayHeight - ((guiTop + y + height) * resolution.getScaleFactor()), width * resolution.getScaleFactor(), height * resolution.getScaleFactor());
        }

        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        int textureSize = 16;

        int countX = (int) Math.ceil((float) width / textureSize);
        int countY = (int) Math.ceil((float) height / textureSize);

        int originX = x + (width - (countX * textureSize)) / 2;
        int originY = y + (height - (countY * textureSize)) / 2;

        for (int spriteY = 0; spriteY < countY; spriteY++) {
            for (int spriteX = 0; spriteX < countX; spriteX++) {
                drawTexturedModalRect(originX + spriteX * textureSize, originY + spriteY * textureSize, sprite, 16, 16);
            }
        }

        if (SCISSOR_AVAILABLE) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    }
}

package net.gegy1000.psf.client.gui;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiContainerMessage extends GuiScreen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(PracticalSpaceFireworks.MODID, "textures/gui/empty_container.png");

    private final ITextComponent title;
    private final ITextComponent[] lines;

    private static final int SIZE_X = 176;
    private static final int SIZE_Y = 166;

    public GuiContainerMessage(ITextComponent title, ITextComponent[] lines) {
        this.title = title;
        this.lines = lines;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        mc.getTextureManager().bindTexture(TEXTURE);

        int minX = (width - SIZE_X) / 2;
        int minY = (height - SIZE_Y) / 2;

        GlStateManager.color(1, 1, 1, 1);
        drawTexturedModalRect(minX, minY, 0, 0, SIZE_X, SIZE_Y);

        String title = this.title.getFormattedText();
        fontRenderer.drawString(title, (width - fontRenderer.getStringWidth(title)) / 2, minY + 6, 0x404040);

        int textLineHeight = fontRenderer.FONT_HEIGHT + 1;
        int textBlockHeight = this.lines.length * textLineHeight;

        int textOriginY = (height - textBlockHeight) / 2;
        for (int i = 0; i < this.lines.length; i++) {
            String line = this.lines[i].getFormattedText();

            int lineX = (width - fontRenderer.getStringWidth(line)) / 2;
            int lineY = textOriginY + textLineHeight * i;
            fontRenderer.drawString(line, lineX, lineY, 0x404040);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1 || mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode)) { 
            mc.player.closeScreen(); 
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}

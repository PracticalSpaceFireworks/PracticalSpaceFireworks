package net.gegy1000.psf.client.gui;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class TankRenderer extends Gui {
    private static final Minecraft CLIENT = Minecraft.getMinecraft();

    private final int x, y;
    private final int width, height;
    private final int screenWidth, screenHeight;

    public void draw(Fluid fluid, int amount, int capacity, int guiLeft, int guiTop) {
        GlStateManager.enableBlend();

        TextureAtlasSprite sprite = CLIENT.getTextureMapBlocks().getAtlasSprite(fluid.getStill().toString());

        float scale = (float) amount / capacity;
        int fluidHeight = (int) (scale * height);
        drawSpriteTiled(sprite, guiLeft + x + 1, guiTop + y + 1 + (height - fluidHeight), width, fluidHeight);

        GlStateManager.disableBlend();
    }
    
    public void drawTooltip(Fluid fluid, int amount, int capacity, int mouseX, int mouseY) {
        if (mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height) {
            List<String> lines = new ArrayList<>();
            lines.add(fluid.getRarity().color + I18n.format(fluid.getUnlocalizedName()));
            lines.add(TextFormatting.BLUE.toString() + amount + "/" + capacity + " mB");

            float percentage = capacity == 0 ? 0 : (float) amount / capacity * 100;
            lines.add(TextFormatting.GRAY.toString() + TextFormatting.ITALIC + String.format("%.1f%%", percentage));

            GuiUtils.drawHoveringText(lines, mouseX, mouseY, screenWidth, screenHeight, -1, CLIENT.fontRenderer);
        }
    }

    private void drawSpriteTiled(TextureAtlasSprite sprite, int x, int y, int width, int height) {
        CLIENT.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        int textureSize = 16;

        int countX = (int) Math.ceil((float) width / textureSize);
        int countY = (int) Math.ceil((float) height / textureSize);
        
        float texWidth = sprite.getMaxU() - sprite.getMinU();
        float texHeight = sprite.getMaxV() - sprite.getMinV();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        for (int spriteY = 0; spriteY < countY; spriteY++) {
            for (int spriteX = 0; spriteX < countX; spriteX++) {
                int startX = spriteX * 16;
                int startY = spriteY * 16;
                int pixelsX = Math.min(textureSize, width - startX);
                int pixelsY = Math.min(textureSize, height - startY);
                int minX = x + startX, maxX = x + startX + pixelsX;
                int minY = y + startY, maxY = y + startY + pixelsY;
                float minU = sprite.getMinU(), maxU = sprite.getMinU() + (texWidth * (pixelsX / (float) textureSize));
                float minV = sprite.getMinV(), maxV = sprite.getMinV() + (texHeight * (pixelsY / (float) textureSize));
                bufferbuilder.pos(minX, maxY, zLevel).tex(minU, maxV).endVertex();
                bufferbuilder.pos(maxX, maxY, zLevel).tex(maxU, maxV).endVertex();
                bufferbuilder.pos(maxX, minY, zLevel).tex(maxU, minV).endVertex();
                bufferbuilder.pos(minX, minY, zLevel).tex(minU, minV).endVertex();
            }
        }
        
        tessellator.draw();
    }
}

package net.gegy1000.psf.server.block.remote;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;

@ParametersAreNonnullByDefault
public abstract class AbstractScrollingList<T> extends GuiScrollingList {
    
    private final List<T> elements;

    public AbstractScrollingList(List<T> elements, Minecraft client, int width, int height, int top, int bottom, int left, int entryHeight, int screenWidth, int screenHeight) {
        super(client, width, height, top, bottom, left, entryHeight, screenWidth, screenHeight);
        this.elements = elements;
    }
    
    protected abstract String getText(T element);
    
    protected T getElement(int index) {
        return elements.get(index);
    }

    @Override
    protected int getSize() {
        return elements.size();
    }

    @Override
    protected boolean isSelected(int index) {
        return false;
    }

    @Override
    protected void drawBackground() {}
    
    protected final boolean isHovering(int slotTop, int entryRight) {
        return mouseY >= slotTop && mouseY < slotTop + slotHeight && mouseX >= entryRight - listWidth && mouseX < entryRight;
    }

    @Override
    protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, @Nullable Tessellator tess) {
        T ele = getElement(slotIdx);
        boolean hovered = isHovering(slotTop, entryRight);
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        fr.drawSplitString(getText(ele), left + slotBuffer, slotTop + (slotHeight / 2) - (fr.FONT_HEIGHT / 2), listWidth - (slotBuffer * 2), hovered ? 0xFFFFFF55 : -1);
    }
}

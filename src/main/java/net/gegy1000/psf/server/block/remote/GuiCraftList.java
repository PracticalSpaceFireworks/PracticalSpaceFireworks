package net.gegy1000.psf.server.block.remote;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;

import java.util.List;

public class GuiCraftList extends GuiScrollingList {
    
    private GuiControlSystem gui;

    public GuiCraftList(GuiControlSystem parent, Minecraft client, int width, int height, int top, int bottom, int left, int entryHeight, int screenWidth, int screenHeight) {
        super(client, width, height, top, bottom, left, entryHeight, screenWidth, screenHeight);
        this.gui = parent;
    }
    
    protected List<IListedSpacecraft> getCrafts() {
        return gui.getContainer().getTe().getCrafts();
    }

    @Override
    protected int getSize() {
        return getCrafts().size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick) {
        gui.selectCraft(index);
    }

    @Override
    protected boolean isSelected(int index) { return false; }

    @Override
    protected void drawBackground() {}

    @Override
    protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
        IListedSpacecraft craft = getCrafts().get(slotIdx);
        boolean hovered = mouseY >= slotTop && mouseY < slotTop + slotHeight && mouseX >= entryRight - listWidth && mouseX < entryRight; 
        gui.mc.fontRenderer.drawSplitString(craft.getName(), left + slotBuffer, slotTop + (slotHeight / 2) - (gui.mc.fontRenderer.FONT_HEIGHT / 2), listWidth - (slotBuffer * 2), hovered ? 0xFFFFFF55 : -1);
    }
}

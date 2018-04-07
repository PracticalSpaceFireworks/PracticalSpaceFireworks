package net.gegy1000.psf.server.block.remote;

import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class GuiCraftList extends AbstractScrollingList<IListedSpacecraft> {
    
    private final GuiSelectCraft parent;

    public GuiCraftList(GuiSelectCraft parent, Minecraft client, int width, int height, int top, int bottom, int left, int entryHeight, int screenWidth, int screenHeight) {
        super(Collections.emptyList(), client, width, height, top, bottom, left, entryHeight, screenWidth, screenHeight);
        this.parent = parent;
    }
    
    @Override
    @Nonnull
    protected String getText(IListedSpacecraft element) {
        return element.getName();
    }
    
    protected List<IListedSpacecraft> getCrafts() {
        return parent.getTe().getCrafts();
    }

    @Override
    protected int getSize() {
        return getCrafts().size();
    }

    @Override
    protected IListedSpacecraft getElement(int index) {
        return getCrafts().get(index);
    }
    
    @Override
    protected void elementClicked(int index, boolean doubleClick) {
        parent.selectCraft(index);
    }
}

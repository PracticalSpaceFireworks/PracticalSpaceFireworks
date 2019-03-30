package net.gegy1000.psf.server.block.remote;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;

@ParametersAreNonnullByDefault
public class GuiCraftList extends AbstractScrollingList<String> {

    private final GuiSelectCraft parent;

    private final List<IListedSpacecraft> crafts;

    private final List<IListedSpacecraft> ground;
    private final List<IListedSpacecraft> space;

    private final List<String> groundEntries;
    private final List<String> spaceEntries;

    public GuiCraftList(GuiSelectCraft parent, Minecraft client, int width, int height, int top, int bottom, int left, int entryHeight, int screenWidth, int screenHeight) {
        super(Collections.emptyList(), client, width, height, top, bottom, left, entryHeight, screenWidth, screenHeight);
        this.parent = parent;

        crafts = parent.getTe().getCrafts();

        ground = crafts.stream().filter(c -> !c.isOrbiting()).filter(c -> !c.isDestroyed()).collect(Collectors.toList());
        space = crafts.stream().filter(IListedSpacecraft::isOrbiting).collect(Collectors.toList());

        groundEntries = ground.stream().map(IListedSpacecraft::getName).collect(Collectors.toList());
        if (!groundEntries.isEmpty()) {
            groundEntries.add(0, TextFormatting.GREEN.toString() + TextFormatting.UNDERLINE + "Ground");
        }

        spaceEntries = space.stream().map(IListedSpacecraft::getName).collect(Collectors.toList());
        if (!spaceEntries.isEmpty()) {
            spaceEntries.add(0, TextFormatting.AQUA.toString() + TextFormatting.UNDERLINE + "Space");
        }
    }

    @Override
    @Nonnull
    protected String getText(String element) {
        return element;
    }

    @Override
    protected String getElement(int index) {
        if (index < groundEntries.size()) {
            return groundEntries.get(index);
        } else {
            return spaceEntries.get(index - groundEntries.size());
        }
    }

    @Override
    protected int getSize() {
        return groundEntries.size() + spaceEntries.size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick) {
        IListedSpacecraft craft = getCraft(index);
        if (craft != null) {
            parent.selectCraft(crafts.indexOf(craft));
        }
    }

    private IListedSpacecraft getCraft(int listIndex) {
        int groundIndex = listIndex;
        int spaceIndex = listIndex - groundEntries.size();

        if (!ground.isEmpty()) {
            groundIndex--;
        }
        if (!space.isEmpty()) {
            spaceIndex--;
        }

        if (groundIndex >= 0 && groundIndex < ground.size()) {
            return ground.get(groundIndex);
        }
        if (spaceIndex >= 0 && spaceIndex < space.size()) {
            return space.get(spaceIndex);
        }

        return null;
    }
}

package net.gegy1000.psf.server.block.remote;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.collect.Lists;

import net.gegy1000.psf.api.IModule;
import net.minecraft.client.Minecraft;

@ParametersAreNonnullByDefault
public class GuiModuleList extends AbstractScrollingList<List<IModule>> {
    
    private static List<List<IModule>> groupModules(List<IModule> modules) {
        modules = Lists.newArrayList(modules); // shallow copy
        List<List<IModule>> ret = new ArrayList<>();
        while (!modules.isEmpty()) {
            List<IModule> bucket = new ArrayList<>();
            IModule identity = modules.remove(0);
            bucket.add(identity);
            for (IModule m : modules) {
                if (identity.groupWith(m)) {
                    bucket.add(m);
                }
            }
            modules.removeAll(bucket);
            ret.add(bucket);
        }
        ret.sort(Comparator.comparing(list -> list.get(0).getLocalizedName()));
        return ret;
    }
    
    private final GuiSelectModule parent;

    public GuiModuleList(GuiSelectModule parent, List<IModule> modules, Minecraft client, int width, int height, int top, int bottom, int left, int entryHeight, int screenWidth, int screenHeight) {
        super(groupModules(modules), client, width, height, top, bottom, left, entryHeight, screenWidth, screenHeight);
        this.parent = parent;
    }
    
    @Override
    @Nonnull
    protected String getText(List<IModule> element) {
        String title = element.get(0).getLocalizedName();
        if (element.size() > 1) {
            title += " (x" + element.size() + ")";
        }
        return title;
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick) {
        parent.selectModuleGroup(getElement(index));
    }
}

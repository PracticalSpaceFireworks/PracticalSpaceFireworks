package net.gegy1000.psf.server.block.remote.config;

import javax.annotation.Nonnull;

import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.IModuleConfig;
import net.gegy1000.psf.api.IModuleConfigButton;
import net.gegy1000.psf.api.IModuleConfigDisplay;
import net.minecraft.client.Minecraft;

public class ModuleConfigButtonToggle extends ModuleConfigButtonAction {

    public ModuleConfigButtonToggle(GuiModuleConfig parent, IModule module, IModuleConfig cfg, int id, int xPos, int yPos, int width, int height) {
        super(parent, module, cfg, id, xPos, yPos, width, height);
    }

    @Override
    public boolean mousePressed(@Nonnull Minecraft mc, int mouseX, int mouseY) {
        boolean ret = super.mousePressed(mc, mouseX, mouseY);
        this.displayString = getConfig().getValue();
        return ret;
    }
    
    public static IModuleConfigDisplay factory(IModuleConfig cfg) {
        return new IModuleConfigDisplay() {

            @Override
            public IModuleConfigButton<?> createButton(GuiModuleConfig parent, IModule module, int id, int x, int y, int width) {
                return new ModuleConfigButtonToggle(parent, module, cfg, id, x, y, width, 20);
            }
        };
    }
}

package net.gegy1000.psf.server.block.remote.config;

import javax.annotation.Nonnull;

import lombok.AccessLevel;
import lombok.Getter;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.IModuleConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.config.GuiButtonExt;

public class ModuleConfigButtonAction extends GuiButtonExt implements IModuleConfigButton<ModuleConfigButtonAction> {

    @Getter(AccessLevel.PROTECTED)
    private final GuiModuleConfig parent;
    
    @Getter(AccessLevel.PROTECTED)
    private final IModule module;
    
    @Getter
    @Nonnull
    private final IModuleConfig config;
    private final int initialY;
    
    public ModuleConfigButtonAction(GuiModuleConfig parent, IModule module, IModuleConfig cfg, int id, int xPos, int yPos, int width, int height) {
        super(id, xPos, yPos, width, height, cfg.getValue());
        this.parent = parent;
        this.module = module;
        this.initialY = yPos;
        this.config = cfg;
    }

    @Override
    public boolean mousePressed(@Nonnull Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            getConfig().modified(null);
            getParent().updateConfig(getModule(), config);
            return true;
        }
        return false;
    }

    @Override
    public @Nonnull ModuleConfigButtonAction getButton() {
        return this;
    }

    @Override
    public int getY() {
        return initialY;
    }
}

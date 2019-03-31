package net.gegy1000.psf.server.block.remote.config;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.lwjgl.input.Mouse;

import lombok.AccessLevel;
import lombok.Getter;
import net.gegy1000.psf.api.module.IModule;
import net.gegy1000.psf.api.module.IModuleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;

@ParametersAreNonnullByDefault
public class ModuleConfigButtonTextField extends GuiTextField implements IModuleConfigButton<GuiButton> {

    @Getter(AccessLevel.PROTECTED)
    private final GuiModuleConfig parent;
    
    @Getter(AccessLevel.PROTECTED)
    private final IModule module;
    
    private final IModuleConfig cfg;
    private final int initialY;
    
    private final GuiButton delegate;

    public ModuleConfigButtonTextField(GuiModuleConfig parent, IModule module, IModuleConfig cfg, int id, FontRenderer fontrendererObj, int x, int y, int width, int height) {
        super(id, fontrendererObj, x, y, width, height);
        this.parent = parent;
        this.module = module;
        this.cfg = cfg;
        this.initialY = y;
        this.delegate = new GuiButton(id, x, y, width, height, "") {
            @Override
            public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
                return ModuleConfigButtonTextField.this.mouseClicked(mouseX, mouseY, Mouse.getEventButton());
            }
            
            @Override
            public void mouseReleased(int mouseX, int mouseY) {}
            
            @Override
            public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
                ModuleConfigButtonTextField.this.y = this.y;
                ModuleConfigButtonTextField.this.drawTextBox();
            }
        };
    }

    @Override
    public boolean textboxKeyTyped(char typedChar, int keyCode) {
        boolean ret = super.textboxKeyTyped(typedChar, keyCode);
        cfg.modified(getText());
        parent.updateConfig(module, cfg);
        return ret;
    }
    
    @Override
    public boolean keyTyped(char typedChar, int keyCode) {
        return textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    @Nonnull
    public GuiButton getButton() {
        return this.delegate;
    }

    @Override
    public int getY() {
        return initialY;
    }
}

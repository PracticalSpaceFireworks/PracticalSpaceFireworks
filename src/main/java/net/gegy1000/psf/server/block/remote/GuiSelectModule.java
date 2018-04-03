package net.gegy1000.psf.server.block.remote;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.input.Mouse;

import com.google.common.collect.Lists;

import lombok.Getter;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.server.block.remote.config.GuiModuleConfig;
import net.minecraft.client.Minecraft;

public class GuiSelectModule extends GuiRemoteControl {
    
    private final GuiRemoteControl parent;
    
    private final int craftIndex;
    
    private final List<IModule> modules;
    
    private GuiModuleList moduleList;

    protected GuiSelectModule(GuiRemoteControl parent, int selectedCraft, Collection<IModule> modules, TileRemoteControlSystem te) {
        super(te);
        this.parent = parent;
        this.craftIndex = selectedCraft;
        this.modules = Lists.newArrayList(modules);
    }

    @Override
    public void initGui() {
        super.initGui();
        
        moduleList = new GuiModuleList(this, modules, mc, xSize - 20, ySize - 10, guiTop + 10, guiTop + ySize - 10, guiLeft + 10, 20, width, height);
    }
    
    @Override
    public void handleMouseInput() throws IOException {
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        super.handleMouseInput();
        if (this.moduleList != null)
            this.moduleList.handleMouseInput(mouseX, mouseY);
    }
  
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        moduleList.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void selectModuleGroup(List<IModule> modules) {
        Minecraft.getMinecraft().displayGuiScreen(new GuiModuleConfig(this, craftIndex, modules, getTe()));
    }
}

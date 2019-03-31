package net.gegy1000.psf.server.block.remote.config;

import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.gegy1000.psf.api.IListedSpacecraft;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.IModuleConfig;
import net.gegy1000.psf.server.block.remote.GuiRemoteControl;
import net.gegy1000.psf.server.block.remote.GuiSelectModule;
import net.gegy1000.psf.server.block.remote.TileRemoteControlSystem;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;

public class GuiModuleConfig extends GuiRemoteControl {
    
    private final GuiSelectModule parent;
    private final int craftIndex;
    private final List<IModule> moduleGroup;
    
    private ModuleConfigButtonManager buttonManager;
    
    private List<IModuleConfigButton<?>> buttons = new ArrayList<>();
    
    public GuiModuleConfig(GuiSelectModule parent, int craftIndex, List<IModule> moduleGroup, TileRemoteControlSystem te) {
        super(parent, te);
        this.parent = parent;
        this.craftIndex = craftIndex;
        this.moduleGroup = moduleGroup;
    }
    
    @Override
    public void initGui() {
        super.initGui();

        buttonManager = new ModuleConfigButtonManager(this, guiLeft + 10, guiTop + 10, xSize - 20, ySize - 20);
        addButton(buttonManager);
        
        for (IModule module : moduleGroup) {
            buttonManager.addSummary(module);
            for (IModuleConfig cfg : module.getConfigs()) {
                buttonManager.create(module, cfg);
            }
            buttonManager.spacer(4);
        }
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!buttonManager.keyTyped(typedChar, keyCode)) {
            super.keyTyped(typedChar, keyCode);
        }
    }
    
    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        
        int scroll = Mouse.getEventDWheel();
        buttonManager.scroll(scroll);
    }
    
    @Override
    protected @Nullable IListedSpacecraft getCraft() {
        return getTe().getCrafts().get(craftIndex);
    }
    
    @Override
    public void updateModule(@Nonnull UUID id, @Nonnull NBTTagCompound tag) {
        for (IModule m : moduleGroup) {
            if (m.getId().equals(id)) {
                m.readUpdateTag(tag);
            }
        }
    }
    
    @Override
    public void removeCraft(UUID id) {
        IListedSpacecraft current = getTe().getCrafts().get(craftIndex);
        if (current == null || current.getId().equals(id)) {
            Minecraft.getMinecraft().displayGuiScreen(parent.getParent());
        }
        super.removeCraft(id);
    }
    
    void updateConfig(IModule module, IModuleConfig cfg) {
        PSFNetworkHandler.network.sendToServer(new PacketConfigChange(getTe().getCrafts().get(craftIndex), module, cfg));
    }
}

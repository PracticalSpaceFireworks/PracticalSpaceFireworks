package net.gegy1000.psf.server.block.remote;

import java.io.IOException;

import org.lwjgl.input.Mouse;

import net.minecraftforge.fml.client.GuiScrollingList;

public class GuiSelectCraft extends GuiRemoteControl {

    private GuiScrollingList craftList;
 
    public GuiSelectCraft(TileRemoteControlSystem te) {
        super(te);
    }
    
    @Override
    public void initGui() {
        super.initGui();

        craftList = new GuiCraftList(this, mc, xSize - 20, ySize - 10, guiTop + 10, guiTop + ySize - 10, guiLeft + 10, 20, width, height);
    }
    
    @Override
    public void handleMouseInput() throws IOException {
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        super.handleMouseInput();
        if (this.craftList != null)
            this.craftList.handleMouseInput(mouseX, mouseY);
    }
  
    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        craftList.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void selectCraft(int index) {
        mc.displayGuiScreen(new GuiCraftDetails(this, index, getTe()));
    }
}

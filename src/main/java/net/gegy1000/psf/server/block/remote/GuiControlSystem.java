package net.gegy1000.psf.server.block.remote;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.lwjgl.input.Mouse;

import lombok.Getter;
import lombok.val;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.client.render.spacecraft.model.SpacecraftModel;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.client.GuiScrollingList;

public class GuiControlSystem extends GuiContainer {
    
    @Nonnull
    private static final ResourceLocation TEXTURE_LOC = new ResourceLocation(PracticalSpaceFireworks.MODID, "textures/gui/control_system.png");
    
    @Getter
    private final ContainerControlSystem container;
    
    private GuiScrollingList craftList;
    
    private int selectedCraft = -1;
    
    private SpacecraftModel model;

    public GuiControlSystem(ContainerControlSystem inventorySlotsIn) {
        super(inventorySlotsIn);
        this.container = inventorySlotsIn;
        
        xSize = 256;
        ySize = 201;
    }
    
    @Override
    public void initGui() {
        super.initGui();

        craftList = new GuiCraftList(this, mc, 180, ySize - 10, guiTop + 10, guiTop + ySize - 10, guiLeft + 10, 20, width, height);
    }
    
    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        super.handleMouseInput();
        if (this.craftList != null)
            this.craftList.handleMouseInput(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawDefaultBackground();
        mc.getTextureManager().bindTexture(TEXTURE_LOC);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        if (selectedCraft >= 0) {
            ISatellite craft = container.getTe().getCrafts().get(selectedCraft);
            GlStateManager.pushMatrix();
            GlStateManager.translate(guiLeft + (ySize / 3), guiTop + (ySize / 2), 100);
            GlStateManager.translate(-8,-8,-8);
            GlStateManager.rotate(-10, 1, 0, 0);
            GlStateManager.rotate(mc.world.getTotalWorldTime() + partialTicks, 0, 1, 0);
            GlStateManager.translate(8, 8, 8);
            GlStateManager.scale(-16, -16, -16);
            
            mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            model.render(BlockRenderLayer.SOLID);

            GlStateManager.enableAlpha();
            model.render(BlockRenderLayer.CUTOUT_MIPPED);
            mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
            model.render(BlockRenderLayer.CUTOUT);
            mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();

            GlStateManager.enableBlend();
            model.render(BlockRenderLayer.TRANSLUCENT);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
            
            int x = guiLeft + (xSize / 2);
            int y = guiTop + 10;
            int color = 0xFF333333;
            mc.fontRenderer.drawString("Modules:", x, y, color);
            x += 10;
            y += 10;
            Map<ResourceLocation, List<IModule>> grouped = craft.getModules().stream().collect(Collectors.groupingBy(IModule::getRegistryName));
            for (val e : grouped.entrySet()) {
                mc.fontRenderer.drawString(e.getKey() + ": " + e.getValue().size(), x, y, color);
                y += 10;
            }
            x -= 10;
            y += 5;
            int energy = craft.getModules().stream()
                    .filter(m -> m instanceof IEnergyStorage)
                    .map(m -> (IEnergyStorage) m)
                    .reduce(0, (e, m) -> e + m.getEnergyStored(), (a, b) -> a + b);
            mc.fontRenderer.drawString("Energy Stored: " + energy, x, y, color);
            
        } else {
            craftList.drawScreen(mouseX, mouseY, partialTicks);
        }
    }

    public void selectCraft(int index) {
        this.selectedCraft = index;
        SpacecraftBuilder builder = new SpacecraftBuilder();
        ISatellite craft = container.getTe().getCrafts().get(index);
        BlockPos origin = craft.getController().getPosition().orElse(BlockPos.ORIGIN);
        for (val e : craft.getComponents().entrySet()) {
            builder.setBlockState(e.getKey().subtract(origin), e.getValue());
        }
        this.model = SpacecraftModel.build(builder.buildBlockAccess(origin, Minecraft.getMinecraft().world));
    }
}

package net.gegy1000.psf.server.block.remote;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.data.ITerrainScan;
import net.gegy1000.psf.client.render.spacecraft.model.SpacecraftModel;
import net.gegy1000.psf.server.block.remote.packet.PacketRequestVisual;
import net.gegy1000.psf.server.capability.CapabilityModuleData;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftMetadata;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftWorldHandler;
import net.gegy1000.psf.server.entity.world.DelegatedWorld;
import net.gegy1000.psf.server.fluid.PSFFluidRegistry;
import net.gegy1000.psf.server.modules.ModuleTerrainScanner;
import net.gegy1000.psf.server.modules.data.EmptyTerrainScan;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.Rectangle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class GuiCraftDetails extends GuiRemoteControl {

    enum PreviewMode {
        CRAFT,
        MAP,;
    }

    @Nonnull
    private static final ResourceLocation PREVIEW_BG = new ResourceLocation(PracticalSpaceFireworks.MODID, "textures/gui/preview_bg.png");

    private static final boolean scissorAvailable = GLContext.getCapabilities().OpenGL20;

    private final int selectedCraft;

    private PreviewMode mode = PreviewMode.CRAFT;

    private GuiButton buttonModules, buttonBack, buttonMode, buttonLaunch;

    private GuiTextField tfName;

    private final Rectangle panel;

    @Nullable
    private SyncedData synced;
    
    @Nonnull
    private Map<Fluid, ResourceAmount> fluidData = new HashMap<>();
    @Nonnull
    private ResourceAmount energyData = new ResourceAmount(1, 1); // Avoid energy warnings before data is synced
    private double mass;

    private MapRenderer mapRenderer;

    protected GuiCraftDetails(GuiSelectCraft parent, int selected, TileRemoteControlSystem te) {
        super(parent, te);
        this.selectedCraft = selected;

        xSize = 256;
        ySize = 201;

        panel = new Rectangle(10, 10, (xSize / 2) - 20, ySize - 20);
    }

    @Override
    public void initGui() {
        super.initGui();

        IListedSpacecraft craft = getCraft();
        if (craft != null && synced == null) {
            PSFNetworkHandler.network.sendToServer(new PacketRequestVisual(craft.getId()));
        }

        buttonModules = new GuiButtonExt(-1, guiLeft + (xSize / 2), guiTop + 34, 115, 20, "Modules");
        addButton(buttonModules);

        buttonBack = new GuiButtonExt(0, guiLeft + xSize - 50 - 10, guiTop + ySize - 20 - 10, 50, 20, "Back");
        addButton(buttonBack);

        buttonMode = new GuiButtonExt(1, guiLeft + panel.getX() + panel.getWidth() - 22, guiTop + panel.getY() + 2, 20, 20, "C");
        addButton(buttonMode);

        buttonLaunch = new GuiButtonExt(2, guiLeft + panel.getX() + panel.getWidth() + 10, guiTop + ySize - 20 - 10, 50, 20, "Launch");
        addButton(buttonLaunch);

        tfName = new GuiTextField(99, mc.fontRenderer, guiLeft + (xSize / 2), guiTop + 10, 115, 20);
        if (craft != null) {
            tfName.setText(craft.getName());
            buttonLaunch.visible = craft.canLaunch();
        }
    }
    
    @Override
    public void updateScreen() {
        super.updateScreen();
        
        SyncedData synced = this.synced;
        if (synced != null) {
            List<IFluidTankProperties> tanks = synced.tankModules.stream()
                    .map(m -> m.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null))
                    .filter(Objects::nonNull)
                    .flatMap(handler -> Arrays.stream(handler.getTankProperties()))
                    .collect(Collectors.toList());
    
            Map<Fluid, ResourceAmount> totalFluid = new HashMap<>();
            for (IFluidTankProperties tank : tanks) {
                FluidStack contents = tank.getContents();
                if (contents != null) {
                    ResourceAmount amount = totalFluid.computeIfAbsent(contents.getFluid(), fluid -> new ResourceAmount());
                    amount.add(contents.amount, tank.getCapacity());
                }
            }
            
            this.fluidData = totalFluid;
            
            energyData = new ResourceAmount();
            synced.modules.stream()
                    .filter(m -> m.hasCapability(CapabilityEnergy.ENERGY, null))
                    .map(m -> m.getCapability(CapabilityEnergy.ENERGY, null))
                    .forEach(storage -> energyData.add(storage.getEnergyStored(), storage.getMaxEnergyStored()));

            mass = synced.metadata.getMass();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (this.tfName != null) {
            this.tfName.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.tfName != null) {
            if (this.tfName.textboxKeyTyped(typedChar, keyCode)) {
                return;
            }
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (selectedCraft >= 0) {
            updateName();
        }
        if (mapRenderer != null) {
            mapRenderer.delete();
        }
    }

    private void updateName() {
        IListedSpacecraft craft = getCraft();
        if (craft != null) {
            craft.setName(tfName.getText());
        }
    }

    @Override
    protected void actionPerformed(@Nonnull GuiButton button) throws IOException {
        super.actionPerformed(button);
        IListedSpacecraft craft = getCraft();
        if (button == buttonModules && synced != null) {
            mc.displayGuiScreen(new GuiSelectModule(this, selectedCraft, synced.modules, getTe()));
        } else if (button == buttonBack) {
            updateName();
            untrack();
            mc.displayGuiScreen(getParent());
        } else if (button == buttonMode) {
            this.mode = PreviewMode.values()[(this.mode.ordinal() + 1) % PreviewMode.values().length];
            buttonMode.displayString = this.mode.name().substring(0, 1);
        } else if (button == buttonLaunch && craft != null && craft.canLaunch()) {
            craft.launch();
            buttonLaunch.visible = craft.canLaunch();
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        IListedSpacecraft craft = getCraft();

        drawBackground(craft);

        if (craft != null && synced != null) {
            renderPreview(synced);
            tfName.drawTextBox();
            drawStats(craft);
        }
    }
    
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        IListedSpacecraft craft = getCraft();
        SyncedData synced = this.synced;
        if (craft != null && !craft.isOrbiting() && synced != null) {
            SpacecraftMetadata metadata = synced.metadata;
            
            int x = panel.getX() + 4;
            int y = panel.getY() + panel.getHeight() - 4;
            
            int width = panel.getWidth() - 8;
            if (metadata.getThrusters().isEmpty()) {
                y -= drawWarning(x, y, width, Collections.singletonList("No Thrusters!"), mouseX, mouseY);
            } else if (metadata.getTotalForce() / metadata.getMass() < (EntitySpacecraft.GRAVITY * 1.25)) {
                y -= drawWarning(x, y, width, Collections.singletonList("Low Thrust!"), mouseX, mouseY);
            }
            
            if (energyData.capacity == 0) {
                y -= drawWarning(x, y, width, Collections.singletonList("No Batteries!"), mouseX, mouseY);
            } else if ((float) energyData.amount / energyData.capacity < 0.25f){
                y -= drawWarning(x, y, width, Collections.singletonList("Low Energy!"), mouseX, mouseY);
            } else if (synced.energyNetUsage < 0) {
                y -= drawWarning(x, y, width, Collections.singletonList("Needs Solar!"), mouseX, mouseY);
            }

            ResourceAmount kerosene = fluidData.get(PSFFluidRegistry.KEROSENE);
            if (kerosene != null && kerosene.capacity > 0) {
                if ((float) kerosene.amount / kerosene.capacity < 0.25f) {
                    y -= drawWarning(x, y, width, Collections.singletonList("Low Kerosene!"), mouseX, mouseY);
                }
            }
            
            ResourceAmount lox = fluidData.get(PSFFluidRegistry.LIQUID_OXYGEN);
            if (lox != null && lox.capacity > 0) {
                if ((float) lox.amount / lox.capacity < 0.25f) {
                    y -= drawWarning(x, y, width, Collections.singletonList("Low LOX!"), mouseX, mouseY);
                }
            }
        }
    }
    
    private int drawWarning(int x, int y, int width, List<String> strings, int mx, int my) {
        GlStateManager.enableBlend();
        mx -= guiLeft;
        my -= guiTop;
        int height = strings.size() * (mc.fontRenderer.FONT_HEIGHT + 2) + 6;
        int alpha = (mx >= x && my <= y && mx <= x + width && my >= y - height ? 0x55 : 0xFF) << 24;
        drawRect(x, y, x + width, y - height, alpha | 0x333333);
        drawRect(x + 1, y - 1, x + width - 1, y - height + 1, alpha | 0xC1AD00);
        mc.getTextureManager().bindTexture(TEXTURE_LOC);
        GlStateManager.enableBlend();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
        GlStateManager.color(1, 1, 1, (alpha >>> 24) / 255f);
        drawTexturedModalRect(x + 3, y - (int) Math.ceil(height / 2f) - 4, 115, 202, 9, 9);
        int sy = (y - height) + 5;
        for (String s : strings) {
            mc.fontRenderer.drawString(s, x + 15, sy, alpha | 0x333333);
            sy += (mc.fontRenderer.FONT_HEIGHT + 2);
        }
        return height + 2;
    }

    private void drawBackground(@Nullable IListedSpacecraft craft) {
        drawRect(guiLeft + panel.getX() - 1, guiTop + panel.getY() - 1, guiLeft + panel.getX() + panel.getWidth() + 1, guiTop + panel.getY() + panel.getHeight() + 1, 0xFF8A8A8A);
        GlStateManager.color(1, 1, 1);

        mc.getTextureManager().bindTexture(PREVIEW_BG);
        GlStateManager.enableBlend();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
        int craftY = craft == null ? 0 : craft.getPosition().getY();
        float alpha = 0;
        if (craftY > 256) {
            alpha = Math.min((craftY - 256) / 500f, 1);
        }
        drawTexturedModalRect(guiLeft + panel.getX(), guiTop + panel.getY(), 0, 0, panel.getWidth(), panel.getHeight());
        GlStateManager.color(1, 1, 1, alpha);
        drawTexturedModalRect(guiLeft + panel.getX(), guiTop + panel.getY(), 128, 0, panel.getWidth(), panel.getHeight());
        GlStateManager.color(1, 1, 1);

        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f);
        GlStateManager.disableBlend();
    }

    private void renderPreview(SyncedData synced) {
        switch (mode) {
            case CRAFT:
                renderCraft(synced.model);
                break;
            case MAP:
                renderMap(synced);
                break;
        }
    }

    private void renderMap(SyncedData syncedData) {
        Optional<ITerrainScan> terrainScan = syncedData.terrainScannerModules.stream()
                .map(module -> module.getCapability(CapabilityModuleData.TERRAIN_SCAN, null))
                .filter(Objects::nonNull)
                .findFirst();

        ITerrainScan buildScan = terrainScan.orElseGet(() -> new EmptyTerrainScan(ModuleTerrainScanner.SCAN_RANGE));
        if (mapRenderer == null || mapRenderer.shouldUpdate(buildScan)) {
            if (mapRenderer != null) {
                mapRenderer.delete();
            }
            mapRenderer = new MapRenderer(buildScan);
        }

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableDepth();

        if (scissorAvailable) {
            ScaledResolution sr = new ScaledResolution(mc);
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor((guiLeft + panel.getX()) * sr.getScaleFactor(), mc.displayHeight - ((guiTop + panel.getY() + panel.getHeight()) * sr.getScaleFactor()),
                    panel.getWidth() * sr.getScaleFactor(), panel.getHeight() * sr.getScaleFactor());
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.translate(guiLeft + (xSize / 4), guiTop + ySize / 2, 500);

        GlStateManager.rotate(-45.0F, 1.0F, 0.0F, 0.0F);
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.rotate(mc.player.ticksExisted + mc.getRenderPartialTicks(), 0, 1, 0);
        GlStateManager.scale(-1.8, -1.8, -1.8);

        GlStateManager.translate(-8.0, 0.0, -8.0);

        mapRenderer.performUploads();
        mapRenderer.render();

        if (scissorAvailable) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }

        GlStateManager.disableDepth();
        GlStateManager.disableRescaleNormal();
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.popMatrix();
    }

    private void renderCraft(SpacecraftModel model) {
        SpacecraftWorldHandler worldHandler = model.getWorldHandler();
        BlockPos minPos = worldHandler.getMinPos();
        BlockPos maxPos = worldHandler.getMaxPos();
        AxisAlignedBB bb = new AxisAlignedBB(new Vec3d(minPos), new Vec3d(maxPos).addVector(1, 1, 1));

        GlStateManager.pushMatrix();

        double lengthX = (bb.maxX - bb.minX) * 16;
        double lengthY = (bb.maxY - bb.minY) * 16;
        double lengthZ = (bb.maxZ - bb.minZ) * 16;

        double halfX = lengthX / 2;
        double halfY = lengthY / 2;
        double halfZ = lengthZ / 2;

        final double maxW = 6 * 16;
        final double maxH = 11 * 16;

        double overW = Math.max(lengthX - maxW, lengthZ - maxW);
        double overH = lengthY - maxH;

        double sc = 1;

        if (overW > 0 && overW >= overH) {
            sc = maxW / (overW + maxW);
        } else if (overH > 0 && overH >= overW) {
            sc = maxH / (overH + maxH);
        }

        halfX *= sc;
        halfY *= sc;
        halfZ *= sc;

        GlStateManager.translate(guiLeft + halfX + (xSize / 4), guiTop + halfY + (ySize / 2), 500);

        GlStateManager.translate(-halfX, -halfY, -halfZ);
        GlStateManager.rotate(-10, 1, 0, 0);
        GlStateManager.rotate(mc.player.ticksExisted + mc.getRenderPartialTicks(), 0, 1, 0);

        GlStateManager.translate(halfX, halfY, halfZ);

        GlStateManager.scale(sc, sc, sc);
        GlStateManager.translate(minPos.getX() * 16, minPos.getY() * 16, minPos.getZ() * 16);

        GlStateManager.scale(-16, -16, -16);

        if (scissorAvailable) {
            ScaledResolution sr = new ScaledResolution(mc);
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor((guiLeft + panel.getX()) * sr.getScaleFactor(), mc.displayHeight - ((guiTop + panel.getY() + panel.getHeight()) * sr.getScaleFactor()),
                    panel.getWidth() * sr.getScaleFactor(), panel.getHeight() * sr.getScaleFactor());
        }

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

        if (scissorAvailable) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
        GlStateManager.popMatrix();
    }

    private void drawStats(IListedSpacecraft craft) {
        int x = guiLeft + (xSize / 2);
        int y = guiTop + 62;
        int color = 0xFF333333;

        boolean orbiting = craft.isOrbiting();
        if (orbiting) {

            mc.fontRenderer.drawString("Energy", x, y, color);
            y += 12;
            drawBar(x, y, energyData.amount, energyData.capacity, 0xFFFFCD4F);
            y += 12;
        } else {
            for (Map.Entry<Fluid, ResourceAmount> entry : fluidData.entrySet()) {
                String localizedName = I18n.format(entry.getKey().getUnlocalizedName());
                mc.fontRenderer.drawString(localizedName, x, y, color);
                y += 12;
                drawBar(x, y, entry.getValue().amount, entry.getValue().capacity, entry.getKey().getColor());
                y += 12;
            }
        }

        mc.fontRenderer.drawString(orbiting ? "Orbiting Over:" : "Position:", x, y, color);
        BlockPos pos = craft.getPosition();
        x += 5;
        y += 10;
        mc.fontRenderer.drawString("X: " + pos.getX(), x, y, color);
        if (!orbiting) {
            y += 10;
            mc.fontRenderer.drawString("Y: " + pos.getY(), x, y, color);
        }
        y += 10;
        mc.fontRenderer.drawString("Z: " + pos.getZ(), x, y, color);
        x -= 5;
        y += 15;
        
        mc.fontRenderer.drawString("Mass: " + DecimalFormat.getInstance().format(mass) + "kg", x, y, color);
    }

    private void drawBar(int x, int y, int value, int max, int color) {
        mc.getTextureManager().bindTexture(TEXTURE_LOC);

        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        float valueScale = (float) value / max;

        drawTexturedModalRect(x, y, 0, 202, 115, 5);
        GlStateManager.color(red / 255.0F, green / 255.0F, blue / 255.0F, 1.0F);
        drawTexturedModalRect(x, y, 0, 207, (int) (valueScale * 115), 5);

        GlStateManager.color(1, 1, 1, 1);
    }

    @Nullable
    @Override
    public IListedSpacecraft getCraft() {
        if (selectedCraft >= 0) {
            return getTe().getCrafts().get(selectedCraft);
        }
        return null;
    }

    @Override
    public void setVisual(@Nonnull IVisual visual) {
        synced = new SyncedData(visual);
    }

    @Override
    public void updateCraft(@Nonnull IListedSpacecraft craft) {
        tfName.setText(craft.getName());
        buttonLaunch.visible = craft.canLaunch();
    }

    @Override
    public void updateModule(@Nonnull UUID id, @Nonnull NBTTagCompound tag) {
        if (synced != null) {
            synced.modules.stream().filter(m -> m.getId().equals(id)).findFirst().ifPresent(m -> m.readUpdateTag(tag));
        }
    }

    private class SyncedData {
        final Collection<IModule> modules;
        final Collection<IModule> terrainScannerModules;
        final Collection<IModule> tankModules;
        final SpacecraftModel model;
        final SpacecraftMetadata metadata;
        final int energyNetUsage;

        public SyncedData(IVisual visual) {
            model = SpacecraftModel.build(new DelegatedWorld(Minecraft.getMinecraft().world, visual.getWorldHandler()), visual.getWorldHandler());
            modules = visual.getModules();
            terrainScannerModules = modules.stream()
                    .filter(module -> module.hasCapability(CapabilityModuleData.TERRAIN_SCAN, null))
                    .collect(Collectors.toList());
            tankModules = modules.stream()
                    .filter(m -> m.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null))
                    .collect(Collectors.toList());
            metadata = visual.getWorldHandler().buildSpacecraftMetadata();

            energyNetUsage = modules.stream()
                    .filter(m -> m.hasCapability(CapabilityModuleData.ENERGY_STATS, null))
                    .map(m -> m.getCapability(CapabilityModuleData.ENERGY_STATS, null))
                    .reduce(0, (val, handler) -> val + handler.getMaxFill() - handler.getMaxDrain(), (a, b) -> a + b);
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    private class ResourceAmount {
        private int capacity;
        private int amount;

        public void add(int amount, int capacity) {
            this.amount += amount;
            this.capacity += capacity;
        }
    }
}

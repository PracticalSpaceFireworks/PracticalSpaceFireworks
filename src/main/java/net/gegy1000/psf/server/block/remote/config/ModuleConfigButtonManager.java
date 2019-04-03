package net.gegy1000.psf.server.block.remote.config;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import com.google.common.collect.ImmutableList;

import javax.annotation.ParametersAreNonnullByDefault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.gegy1000.psf.api.module.IModule;
import net.gegy1000.psf.api.module.IModuleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;

@ParametersAreNonnullByDefault
public class ModuleConfigButtonManager extends GuiButton {
    
    @Getter
    @RequiredArgsConstructor
    private class TextHolder {
        
        private final List<String> text;
        private final int x, y;
        private final int height;
        @Accessors(fluent = true)
        private final boolean centerFirstLine;
        
        public TextHolder(IModule module, int x, int y) {
            this(new ImmutableList.Builder<String>().add(module.getLocalizedName()).addAll(module.getSummary()).build(), x, y, (module.getSummary().size() + 1) * lineHeight, true);
        }
    }
    
    private static final boolean scissorAvailable = GLContext.getCapabilities().OpenGL20;
    
    private final GuiModuleConfig parent;
        
    private final int startX, startY, width, height;
    
    private final int edgePadding = 5;
    
    private final int linePadding = 2;
    private final int lineHeight = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT + linePadding;
    
    public ModuleConfigButtonManager(GuiModuleConfig parent, int startX, int startY, int width, int height) {
        super(-42, startX, startY, width, height, "");
        this.parent = parent;
        this.startX = startX + edgePadding;
        this.startY = startY + edgePadding;
        this.width = width - (edgePadding * 2);
        this.height = height - (edgePadding * 2);
    }
    
    private int id;
    private int y;
    
    private int scroll;
    
    private final List<TextHolder> summaries = new ArrayList<>();
    private final List<IModuleConfigButton<?>> buttons = new ArrayList<>();
    
    public IModuleConfigButton<?> create(IModule module, IModuleConfig cfg) {
        if (cfg.getType() != IModuleConfig.ConfigType.ACTION) {
            String key = module.getUnlocalizedName() + ".config." + cfg.getKey();
            summaries.add(new TextHolder(Collections.singletonList(I18n.format(key) + ":"), startX, startY + y, 8, false));
            y += 8;
        }
        IModuleConfigButton<?> btn;
        switch (cfg.getType()) {
        default:
        case ACTION:
            btn = new ModuleConfigButtonAction(parent, module, cfg, id++, startX, startY + y, width, 20);
            break;
        case TOGGLE:
            btn = new ModuleConfigButtonToggle(parent, module, cfg, id, startX, startY + y, width, 20);
            break;
        case TEXT:
            btn = new ModuleConfigButtonTextField(parent, module, cfg, id, Minecraft.getMinecraft().fontRenderer, startX, startY + y, width, 20);
            break;                
        }
        buttons.add(btn);
        y += btn.getButton().height + linePadding;
        return btn;
    }
    
    public void addSummary(IModule module) {
        TextHolder holder = new TextHolder(module, startX, startY + y);
        summaries.add(holder);
        y += holder.height + linePadding;
    }
    
    public void spacer(int amount) {
        y += amount;
    }

    public void scroll(int delta) {
        this.scroll = MathHelper.clamp(this.scroll - (int) ((delta / 120F) * 4), 0, Math.max(y - height, 0));
    }
    
    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        
        drawRect(startX - edgePadding, startY - edgePadding, startX + width + edgePadding, startY + height + edgePadding, 0x99999999);
        
        if (scissorAvailable) {
            ScaledResolution sr = new ScaledResolution(mc);
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor(startX * sr.getScaleFactor(), mc.displayHeight - ((startY + height) * sr.getScaleFactor()), width * sr.getScaleFactor(), height * sr.getScaleFactor());
        }

        for (IModuleConfigButton<?> btn : buttons) {
            GuiButton b = btn.getButton();
            if (btn.getY() - scroll + b.height > startY && btn.getY() - scroll < startY + height) {
                b.y = btn.getY() - scroll;
                b.drawButton(mc, mouseX, mouseY, partialTicks);
            }
        }
        for (TextHolder holder : summaries) {
            if (holder.getY() - scroll + holder.getHeight() > startY && holder.getY() - scroll < startY + height) {
                int renderY = holder.getY() - scroll;
                boolean first = true;
                for (String s : holder.getText()) {
                    if (first && holder.centerFirstLine()) { 
                        mc.fontRenderer.drawString(s, holder.getX() + (width / 2) - (mc.fontRenderer.getStringWidth(s) / 2), renderY, 0xFF000000);
                    } else {
                        mc.fontRenderer.drawString(s, holder.getX(), renderY, 0xFF000000);
                    }
                    renderY += lineHeight;
                    first = false;
                }
            }
        }
        
        if (scissorAvailable) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    }

    protected final Optional<IModuleConfigButton<?>> getHovered(int mouseX, int mouseY) {
        for (IModuleConfigButton<?> btn : buttons) {
            GuiButton b = btn.getButton();
            if (mouseX >= b.x && mouseX <= b.x + b.width && mouseY >= b.y && mouseY <= b.y + b.height) {
                return Optional.of(btn);
            }
        }
        return Optional.empty();
    }
    
    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        boolean ret = false;
        for (IModuleConfigButton<?> btn : buttons) {
            ret |= btn.getButton().mousePressed(mc, mouseX, mouseY);
        }
        return ret;
    }
    
    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        for (IModuleConfigButton<?> button : buttons) {
            button.getButton().mouseReleased(mouseX, mouseY);
        }
    }

    public boolean keyTyped(char typedChar, int keyCode) {
        boolean ret = false;
        for (IModuleConfigButton<?> btn : buttons) {
            ret |= btn.keyTyped(typedChar, keyCode);
        }
        return ret;
    }
}

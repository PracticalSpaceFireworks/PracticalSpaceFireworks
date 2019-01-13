package net.gegy1000.psf.server.block.remote.widget;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import java.util.function.DoubleSupplier;

import net.gegy1000.psf.client.gui.IWidgetIcon;
import net.gegy1000.psf.client.gui.PSFIcons;
import net.gegy1000.psf.client.gui.Widget;
import net.minecraft.client.renderer.GlStateManager;

@ParametersAreNonnullByDefault
public class WidgetLabeledBar extends Widget {
    
    private final @Nonnull String label;
    private final int color;
    private final @Nonnull DoubleSupplier state;

    public WidgetLabeledBar(String label, int color, DoubleSupplier state, int x, int y) {
        super(x, y, PSFIcons.BAR_BACKGROUND.getWidth(), 17);
        this.label = label;
        this.color = color;
        this.state = state;
    }
    
    @Override
    public void draw() {
        mc.fontRenderer.drawString(label, x, y, 0xFF333333);
        drawBar(x, y + mc.fontRenderer.FONT_HEIGHT);
    }
    
    private void drawBar(int x, int y) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        double valueScale = state.getAsDouble();

        PSFIcons.map.render(PSFIcons.BAR_BACKGROUND, x, y, true);
        GlStateManager.color(red / 255.0F, green / 255.0F, blue / 255.0F, 1.0F);
        
        IWidgetIcon bar = PSFIcons.BAR_FILL;
        drawTexturedModalRect(x + 1, y + 1, bar.getX(), bar.getY(), bar.getX() + (int) (valueScale * bar.getWidth()), bar.getHeight());

        GlStateManager.color(1, 1, 1, 1);
    }
}

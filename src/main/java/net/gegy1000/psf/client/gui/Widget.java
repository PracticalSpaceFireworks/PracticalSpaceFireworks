package net.gegy1000.psf.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

@RequiredArgsConstructor
@Getter
public abstract class Widget extends Gui {
    
    public final Minecraft mc = Minecraft.getMinecraft();
    
    protected final int x, y, width, height;
    
    private final List<String> tooltip = new ArrayList<>();
    
    public void setTooltip(String... tooltip) {
        this.tooltip.clear();
        Collections.addAll(this.tooltip, tooltip);
    }
    
    public abstract void draw();
    
    public boolean hovering(int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;
    }
    
    public boolean isVisible() {
        return true;
    }
}

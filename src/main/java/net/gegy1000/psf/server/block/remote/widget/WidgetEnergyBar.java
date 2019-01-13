package net.gegy1000.psf.server.block.remote.widget;

import java.util.Arrays;
import java.util.List;

import net.gegy1000.psf.server.block.remote.ResourceAmount;

public class WidgetEnergyBar extends WidgetLabeledBar {
    
    private final ResourceAmount res;

    public WidgetEnergyBar(ResourceAmount energy, int x, int y) {
        super("Energy", 0xFFFFCD4F, () -> energy.getAmount() / (double) energy.getCapacity(), x, y);
        this.res = energy;
    }
    
    @Override
    public List<String> getTooltip() {
        return Arrays.asList(res.getAmount() + "/" + res.getCapacity() + " FE");
    }
}

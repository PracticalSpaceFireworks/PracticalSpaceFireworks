package net.gegy1000.psf.server.block.remote.widget;

import java.util.Arrays;
import java.util.List;

import net.gegy1000.psf.server.block.remote.ResourceAmount;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fluids.Fluid;

public class WidgetFluidBar extends WidgetLabeledBar {
    
    private final ResourceAmount res;

    public WidgetFluidBar(Fluid fluid, ResourceAmount res, int color, int x, int y) {
        super(I18n.format(fluid.getUnlocalizedName()), color, () -> res.getAmount() / (double) res.getCapacity(), x, y);
        this.res = res;
    }
    
    @Override
    public List<String> getTooltip() {
        return Arrays.asList(res.getAmount() + "/" + res.getCapacity() + " mB");
    }
}

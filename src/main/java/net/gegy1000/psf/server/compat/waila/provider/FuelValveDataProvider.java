package net.gegy1000.psf.server.compat.waila.provider;

import mcp.MethodsReturnNonnullByDefault;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class FuelValveDataProvider implements IWailaDataProvider {
    private static final String TRANSLATION_KEY = "tooltip.psf.waila.fuel_valve";

    @Override
    public List<String> getWailaBody(ItemStack stack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler cfg) {
        tooltip.add(new TextComponentTranslation(TRANSLATION_KEY)
            .setStyle(new Style().setColor(TextFormatting.GOLD))
            .getFormattedText());
        return tooltip;
    }
}

package net.gegy1000.psf.server.fluid;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import java.awt.Color;

import static net.gegy1000.psf.PracticalSpaceFireworks.namespace;

public class FluidAir extends Fluid {
    private static final ResourceLocation STILL = namespace("blocks/air_still");
    private static final ResourceLocation FLOWING = namespace("blocks/air_flow");

    public FluidAir(@Nonnull final String type) {
        super(type + "_air", STILL, FLOWING, new Color(212, 227, 248));
        setUnlocalizedName(namespace(type + "_air", '.'));
        setDensity(1);
        setViscosity(1);
        setGaseous(true);
    }
}

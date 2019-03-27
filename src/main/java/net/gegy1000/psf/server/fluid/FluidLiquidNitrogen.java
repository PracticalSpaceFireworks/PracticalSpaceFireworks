package net.gegy1000.psf.server.fluid;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

import java.awt.Color;

import static net.gegy1000.psf.PracticalSpaceFireworks.namespace;

public class FluidLiquidNitrogen extends Fluid {
    private static final ResourceLocation STILL = namespace("blocks/liquid_nitrogen_still");
    private static final ResourceLocation FLOWING = namespace("blocks/liquid_nitrogen_flow");

    public FluidLiquidNitrogen() {
        super("liquid_nitrogen", STILL, FLOWING, new Color(103, 175, 188));
        setUnlocalizedName(namespace("liquid_nitrogen", '.'));
        setDensity(1141);
        setViscosity(1141);
    }
}

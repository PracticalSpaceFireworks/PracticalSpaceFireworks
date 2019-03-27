package net.gegy1000.psf.server.fluid;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

import java.awt.Color;

import static net.gegy1000.psf.PracticalSpaceFireworks.namespace;

public class FluidLiquidOxygen extends Fluid {
    private static final ResourceLocation STILL = namespace("blocks/liquid_oxygen_still");
    private static final ResourceLocation FLOWING = namespace("blocks/liquid_oxygen_flow");

    public FluidLiquidOxygen() {
        super("liquid_oxygen", STILL, FLOWING, new Color(103, 175, 188));
        setUnlocalizedName(namespace("liquid_oxygen", '.'));
        setDensity(1141);
        setViscosity(1141);
    }
}

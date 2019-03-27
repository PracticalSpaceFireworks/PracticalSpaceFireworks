package net.gegy1000.psf.server.fluid;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

import java.awt.Color;

import static net.gegy1000.psf.PracticalSpaceFireworks.namespace;

public class FluidKerosene extends Fluid {
    private static final ResourceLocation STILL = namespace("blocks/kerosene_still");
    private static final ResourceLocation FLOWING = namespace("blocks/kerosene_flow");

    public FluidKerosene() {
        super("kerosene", STILL, FLOWING, new Color(248, 190, 74));
        setUnlocalizedName(namespace("kerosene", '.'));
        setDensity(810);
        setViscosity(810);
    }
}

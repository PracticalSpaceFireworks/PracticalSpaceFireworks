package net.gegy1000.psf.server.fluid;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

import java.awt.Color;

public class FluidAir extends Fluid {
    private static final ResourceLocation STILL = new ResourceLocation(PracticalSpaceFireworks.MODID, "blocks/air_still");
    private static final ResourceLocation FLOWING = new ResourceLocation(PracticalSpaceFireworks.MODID, "blocks/air_flow");

    public FluidAir(String type) {
        super(type + "_air", STILL, FLOWING, new Color(212, 227, 248));
        this.setUnlocalizedName(PracticalSpaceFireworks.MODID + "." + type + "_air");
        this.setDensity(1);
        this.setViscosity(1);
    }
}

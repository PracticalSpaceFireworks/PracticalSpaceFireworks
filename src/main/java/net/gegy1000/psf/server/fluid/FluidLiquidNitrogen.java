package net.gegy1000.psf.server.fluid;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

import java.awt.Color;

public class FluidLiquidNitrogen extends Fluid {
    private static final ResourceLocation STILL = new ResourceLocation(PracticalSpaceFireworks.MODID, "blocks/liquid_nitrogen_still");
    private static final ResourceLocation FLOWING = new ResourceLocation(PracticalSpaceFireworks.MODID, "blocks/liquid_nitrogen_flow");

    public FluidLiquidNitrogen() {
        super("liquid_nitrogen", STILL, FLOWING, new Color(103, 175, 188));
        this.setUnlocalizedName(PracticalSpaceFireworks.MODID + ".liquid_nitrogen");
        this.setDensity(1141);
        this.setViscosity(1141);
    }
}

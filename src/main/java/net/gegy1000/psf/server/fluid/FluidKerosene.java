package net.gegy1000.psf.server.fluid;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

import java.awt.Color;

public class FluidKerosene extends Fluid {
    private static final ResourceLocation STILL = new ResourceLocation(PracticalSpaceFireworks.MODID, "blocks/kerosene_still");
    private static final ResourceLocation FLOWING = new ResourceLocation(PracticalSpaceFireworks.MODID, "blocks/kerosene_flow");

    public FluidKerosene() {
        super("kerosene", STILL, FLOWING, new Color(248, 190, 74));
        this.setUnlocalizedName(PracticalSpaceFireworks.MODID + ".kerosene");
        this.setDensity(810);
        this.setViscosity(810);
    }
}

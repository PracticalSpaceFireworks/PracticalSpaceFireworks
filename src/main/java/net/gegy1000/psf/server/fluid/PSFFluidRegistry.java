package net.gegy1000.psf.server.fluid;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class PSFFluidRegistry {
    public static final Fluid KEROSENE = new FluidKerosene();
    public static final Fluid LIQUID_OXYGEN = new FluidLiquidOxygen();
    public static final Fluid LIQUID_NITROGEN = new FluidLiquidNitrogen();

    public static final Fluid FILTERED_AIR = new FluidAir("filtered");
    public static final Fluid COMPRESSED_AIR = new FluidAir("compressed");

    static {
        FILTERED_AIR.setGaseous(true);
    }

    public static void register() {
        FluidRegistry.registerFluid(KEROSENE);
        FluidRegistry.addBucketForFluid(KEROSENE);

        FluidRegistry.registerFluid(LIQUID_OXYGEN);
        FluidRegistry.addBucketForFluid(LIQUID_OXYGEN);

        FluidRegistry.registerFluid(LIQUID_NITROGEN);
        FluidRegistry.addBucketForFluid(LIQUID_NITROGEN);

        FluidRegistry.registerFluid(FILTERED_AIR);
        FluidRegistry.registerFluid(COMPRESSED_AIR);
    }
}

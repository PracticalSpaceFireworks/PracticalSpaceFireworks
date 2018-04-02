package net.gegy1000.psf.server.fluid;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class PSFFluidRegistry {
    public static final Fluid KEROSENE = new FluidKerosene();
    public static final Fluid LIQUID_OXYGEN = new FluidLiquidOxygen();

    public static void register() {
        FluidRegistry.registerFluid(KEROSENE);
        FluidRegistry.addBucketForFluid(KEROSENE);

        FluidRegistry.registerFluid(LIQUID_OXYGEN);
        FluidRegistry.addBucketForFluid(LIQUID_OXYGEN);
    }
}

package net.gegy1000.psf.server.init;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.gegy1000.psf.server.fluid.FluidAir;
import net.gegy1000.psf.server.fluid.FluidKerosene;
import net.gegy1000.psf.server.fluid.FluidLiquidNitrogen;
import net.gegy1000.psf.server.fluid.FluidLiquidOxygen;
import net.minecraftforge.fluids.Fluid;

import static com.google.common.base.Preconditions.checkState;
import static net.minecraftforge.fluids.FluidRegistry.addBucketForFluid;
import static net.minecraftforge.fluids.FluidRegistry.registerFluid;

@Accessors(fluent = true)
public final class PSFFluids {
    @Getter private static Fluid kerosene;
    @Getter private static Fluid liquidOxygen;
    @Getter private static Fluid liquidNitrogen;
    @Getter private static Fluid filteredAir;
    @Getter private static Fluid compressedAir;

    private static boolean initialized;

    private PSFFluids() {
        throw new UnsupportedOperationException();
    }

    public static void init() {
        checkState(!initialized, "Already initialized!");

        kerosene = new FluidKerosene();
        liquidOxygen = new FluidLiquidOxygen();
        liquidNitrogen = new FluidLiquidNitrogen();
        filteredAir = new FluidAir("filtered");
        compressedAir = new FluidAir("compressed");

        registerFluid(kerosene);
        addBucketForFluid(kerosene);

        registerFluid(liquidOxygen);
        addBucketForFluid(liquidOxygen);

        registerFluid(liquidNitrogen);
        addBucketForFluid(liquidNitrogen);

        registerFluid(filteredAir);
        registerFluid(compressedAir);

        initialized = true;
    }
}

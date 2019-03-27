package net.gegy1000.psf.server.init;

import net.gegy1000.psf.server.fluid.FluidAir;
import net.gegy1000.psf.server.fluid.FluidKerosene;
import net.gegy1000.psf.server.fluid.FluidLiquidNitrogen;
import net.gegy1000.psf.server.fluid.FluidLiquidOxygen;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static net.minecraftforge.fluids.FluidRegistry.addBucketForFluid;
import static net.minecraftforge.fluids.FluidRegistry.registerFluid;

public final class PSFFluids {
    @Nullable
    private static Fluid kerosene;

    @Nullable
    private static Fluid liquidOxygen;

    @Nullable
    private static Fluid liquidNitrogen;

    @Nullable
    private static Fluid filteredAir;

    @Nullable
    private static Fluid compressedAir;

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

    @Nonnull
    public static Fluid kerosene() {
        return checkNotNull(kerosene);
    }

    @Nonnull
    public static Fluid liquidOxygen() {
        return checkNotNull(liquidOxygen);
    }

    @Nonnull
    public static Fluid liquidNitrogen() {
        return checkNotNull(liquidNitrogen);
    }

    @Nonnull
    public static Fluid filteredAir() {
        return checkNotNull(filteredAir);
    }

    @Nonnull
    public static Fluid compressedAir() {
        return checkNotNull(compressedAir);
    }
}

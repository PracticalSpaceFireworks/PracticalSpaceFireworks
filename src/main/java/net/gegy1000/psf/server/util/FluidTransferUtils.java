package net.gegy1000.psf.server.util;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidTransferUtils {
    public static int transfer(IFluidHandler source, IFluidHandler target, int amount) {
        FluidStack drained = source.drain(amount, true);
        if (drained != null && drained.amount > 0) {
            int filled = target.fill(drained, true);
            FluidStack remaining = new FluidStack(drained.getFluid(), drained.amount - filled);
            if (remaining.amount > 0) {
                source.fill(remaining, true);
            }
            return filled;
        }
        return 0;
    }
}

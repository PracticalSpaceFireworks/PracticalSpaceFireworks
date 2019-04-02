package net.gegy1000.psf.server.capability;

import javax.annotation.Nullable;

import java.util.Collection;
import java.util.Collections;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class MultiTankFluidHandler extends MultiFluidHandler<FluidTank> implements IFluidHandler {
    
    public static final MultiTankFluidHandler EMPTY = new MultiTankFluidHandler(Collections.emptyList());
        
    public MultiTankFluidHandler(Collection<FluidTank> internal) {
        super(internal);
    }

    public int fillInternal(FluidStack resource, boolean doFill) {
        return fillInternal(resource, doFill, FluidTank::fillInternal);
    }
    
    @Nullable
    public FluidStack drainInternal(FluidStack resource, boolean doDrain) {
        return drainInternal(resource, doDrain, FluidTank::drainInternal);
    }

    @Nullable
    public FluidStack drainInternal(int maxDrain, boolean doDrain) {
        return drainInternal(maxDrain, doDrain, FluidTank::drainInternal);
    }
}

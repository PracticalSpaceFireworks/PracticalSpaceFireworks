package net.gegy1000.psf.server.capability;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.FluidTankPropertiesWrapper;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

import lombok.RequiredArgsConstructor;

public class TypedFluidTank extends FluidTank {
    
    @RequiredArgsConstructor
    public enum IO {
        IN(true, false),
        OUT(false, true),
        BOTH(true, true),
        NONE(false, false),
        ;
        
        private final boolean allowIn, allowOut;
    }
    
    private final Fluid filterFluid;
    private final IO ioMode;
    
    public TypedFluidTank(int capacity, Fluid filterFluid) {
        this(capacity, filterFluid, IO.BOTH);
    }

    public TypedFluidTank(int capacity, Fluid filterFluid, IO io) {
        super(new FluidStack(filterFluid, 0), capacity);
        this.filterFluid = filterFluid;
        this.ioMode = io;
    }
    
    @Override
    public boolean canFill() {
        return super.canFill() && ioMode.allowIn;
    }
    
    @Override
    public boolean canDrain() {
        return super.canDrain() && ioMode.allowOut;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        if (this.tankProperties == null) {
            FluidTankPropertiesWrapper properties = new FluidTankPropertiesWrapper(this) {
                @Override
                public FluidStack getContents() {
                    FluidStack contents = super.getContents();
                    if (contents == null) {
                        return new FluidStack(filterFluid, 0);
                    }
                    return contents;
                }
            };
            this.tankProperties = new IFluidTankProperties[] { properties };
        }
        return this.tankProperties;
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid) {
        return fluid.getFluid() == this.filterFluid && canFill();
    }

    @Override
    public boolean canDrainFluidType(@Nullable FluidStack fluid) {
        return fluid == null || fluid.getFluid() == this.filterFluid && canDrain();
    }
}

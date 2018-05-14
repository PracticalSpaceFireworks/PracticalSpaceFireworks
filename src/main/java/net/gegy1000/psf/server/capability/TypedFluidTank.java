package net.gegy1000.psf.server.capability;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.FluidTankPropertiesWrapper;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public class TypedFluidTank extends FluidTank {
    private final Fluid filterFluid;

    public TypedFluidTank(int capacity, Fluid filterFluid) {
        super(new FluidStack(filterFluid, 0), capacity);
        this.filterFluid = filterFluid;
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
        return fluid.getFluid() == this.filterFluid;
    }

    @Override
    public boolean canDrainFluidType(@Nullable FluidStack fluid) {
        return fluid == null || fluid.getFluid() == this.filterFluid;
    }
}

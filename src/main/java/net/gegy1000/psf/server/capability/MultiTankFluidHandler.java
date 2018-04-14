package net.gegy1000.psf.server.capability;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiTankFluidHandler implements IFluidHandler {
    private final List<IFluidHandler> internal;

    public MultiTankFluidHandler(List<IFluidHandler> internal) {
        this.internal = internal;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        Map<Fluid, Properties> collectedProperties = new HashMap<>();
        for (IFluidHandler handler : this.internal) {
            IFluidTankProperties[] allInternalProps = handler.getTankProperties();
            for (IFluidTankProperties internalProp : allInternalProps) {
                FluidStack contents = internalProp.getContents();
                if (contents != null) {
                    Properties merged = collectedProperties.computeIfAbsent(contents.getFluid(), Properties::new);
                    merged.add(contents.amount, internalProp.getCapacity());
                }
            }
        }
        return collectedProperties.values().toArray(new IFluidTankProperties[0]);
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount <= 0) {
            return 0;
        }

        resource = resource.copy();

        int result = 0;

        for (IFluidHandler handler : internal) {
            int amount = handler.fill(resource, doFill);
            result += amount;
            resource.amount -= amount;

            if (resource.amount <= 0) {
                break;
            }
        }

        return result;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if (resource == null || resource.amount <= 0) {
            return null;
        }

        resource = resource.copy();

        FluidStack result = null;
        for (IFluidHandler handler : internal) {
            FluidStack drain = handler.drain(resource, doDrain);
            if (drain != null) {
                if (result == null) {
                    result = drain;
                } else {
                    result.amount += drain.amount;
                }

                resource.amount -= drain.amount;
                if (resource.amount <= 0) {
                    break;
                }
            }
        }

        return result;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        if (maxDrain == 0) {
            return null;
        }

        FluidStack result = null;
        for (IFluidHandler handler : internal) {
            if (result == null) {
                result = handler.drain(maxDrain, doDrain);
                if (result != null) {
                    maxDrain -= result.amount;
                }
            } else {
                FluidStack drainRequest = result.copy();
                drainRequest.amount = maxDrain;
                FluidStack drain = handler.drain(drainRequest, doDrain);
                if (drain != null) {
                    result.amount += drain.amount;
                    maxDrain -= drain.amount;
                }
            }

            if (maxDrain <= 0) {
                break;
            }
        }

        return result;
    }

    private class Properties implements IFluidTankProperties {
        private final Fluid type;
        private final FluidStack contents;
        private int capacity;

        private Properties(Fluid type) {
            this.type = type;
            this.contents = new FluidStack(type, 0);
        }

        public void add(int amount, int capacity) {
            this.contents.amount += amount;
            this.capacity += capacity;
        }

        @Nullable
        @Override
        public FluidStack getContents() {
            return contents;
        }

        @Override
        public int getCapacity() {
            return capacity;
        }

        @Override
        public boolean canFill() {
            return true;
        }

        @Override
        public boolean canDrain() {
            return true;
        }

        @Override
        public boolean canFillFluidType(FluidStack stack) {
            return stack.getFluid() == type;
        }

        @Override
        public boolean canDrainFluidType(FluidStack stack) {
            return stack.getFluid() == type;
        }
    }
}

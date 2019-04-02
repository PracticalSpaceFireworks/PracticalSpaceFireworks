package net.gegy1000.psf.server.capability;

import javax.annotation.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class MultiFluidHandler<T extends IFluidHandler> implements IFluidHandler {
    
    private static final MultiFluidHandler<?> EMPTY = new MultiFluidHandler<>(Collections.emptyList());
    
    @SuppressWarnings("unchecked")
    public static <T extends IFluidHandler> MultiFluidHandler<T> empty() { return (MultiFluidHandler<T>) EMPTY; }
    
    protected final Collection<? extends T> internal;

    public MultiFluidHandler(Collection<? extends T> handlers) {
        this.internal = handlers;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        Map<Fluid, Properties> collectedProperties = new HashMap<>();
        int emptyCapacity = 0;
        for (IFluidHandler handler : this.internal) {
            IFluidTankProperties[] allInternalProps = handler.getTankProperties();
            for (IFluidTankProperties internalProp : allInternalProps) {
                FluidStack contents = internalProp.getContents();
                if (contents != null) {
                    Properties merged = collectedProperties.computeIfAbsent(contents.getFluid(), Properties::new);
                    merged.add(contents.amount, internalProp.getCapacity());
                } else {
                    emptyCapacity += internalProp.getCapacity();
                }
            }
        }
        if (collectedProperties.isEmpty() && emptyCapacity > 0) {
            return new IFluidTankProperties[] { new FluidTankProperties(null, emptyCapacity) };
        }
        return collectedProperties.values().toArray(new IFluidTankProperties[0]);
    }

    @FunctionalInterface
    protected interface Filler<T extends IFluidHandler> {

        int fill(T handler, FluidStack resource, boolean doFill);
    }
    
    @Override
    public int fill(FluidStack resource, boolean doFill) {
        return fillInternal(resource, doFill, IFluidHandler::fill);
    }

    protected int fillInternal(FluidStack resource, boolean doFill, Filler<T> filler) {
        if (resource == null || resource.amount <= 0) {
            return 0;
        }
    
        resource = resource.copy();
    
        int result = 0;
    
        for (T handler : internal) {
            int amount = filler.fill(handler, resource, doFill);
            result += amount;
            resource.amount -= amount;
    
            if (resource.amount <= 0) {
                break;
            }
        }
    
        return result;
    }

    @FunctionalInterface
    protected interface StackDrainer<T extends IFluidHandler> {

        FluidStack drain(T handler, FluidStack resource, boolean doFill);
    }
    
    @Override
    @Nullable
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        return drainInternal(resource, doDrain, IFluidHandler::drain);
    }

    @Nullable
    public FluidStack drainInternal(FluidStack resource, boolean doDrain, StackDrainer<T> drainer) {
        if (resource == null || resource.amount <= 0) {
            return null;
        }
    
        resource = resource.copy();
    
        FluidStack result = null;
        for (T handler : internal) {
            FluidStack drain = drainer.drain(handler, resource, doDrain);
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

    @FunctionalInterface
    protected interface IntDrainer<T extends IFluidHandler> {

        FluidStack drain(T handler, int maxDrain, boolean doFill);
    }

    @Override
    @Nullable
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return drainInternal(maxDrain, doDrain, IFluidHandler::drain);
    }
    
    @Nullable
    public FluidStack drainInternal(int maxDrain, boolean doDrain, IntDrainer<T> drainer) {
        if (maxDrain == 0) {
            return null;
        }
    
        FluidStack result = null;
        for (T handler : internal) {
            if (result == null) {
                result = drainer.drain(handler, maxDrain, doDrain);
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

    class Properties implements IFluidTankProperties {
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

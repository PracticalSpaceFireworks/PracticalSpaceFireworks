package net.gegy1000.psf.server.modules;

import com.google.common.collect.Lists;
import net.gegy1000.psf.api.module.IAdditionalMass;
import net.gegy1000.psf.api.module.ModuleCapabilities;
import net.gegy1000.psf.server.init.PSFFluids;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.templates.FluidHandlerFluidMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class ModuleFuelTank extends EmptyModule {
    private static final int CAPACITY = 500;

    private final FluidTank keroseneTank;
    private final FluidTank liquidOxygenTank;
    private final FuelFluidHandler storage;

    public ModuleFuelTank() {
        super("fuel_tank");

        this.keroseneTank = new FluidTank(PSFFluids.KEROSENE.getFluid(), 0, CAPACITY);
        this.liquidOxygenTank = new FluidTank(PSFFluids.LIQUID_OXYGEN.getFluid(), 0, CAPACITY);
        this.storage = new FuelFluidHandler();
        this.storage.addHandler(PSFFluids.KEROSENE.getFluid(), this.keroseneTank);
        this.storage.addHandler(PSFFluids.LIQUID_OXYGEN.getFluid(), this.liquidOxygenTank);
    }

    public void setFull() {
        this.keroseneTank.fill(new FluidStack(PSFFluids.KEROSENE.getFluid(), CAPACITY), true);
        this.liquidOxygenTank.fill(new FluidStack(PSFFluids.LIQUID_OXYGEN.getFluid(), CAPACITY), true);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = super.serializeNBT();

        Capability<IFluidHandler> cap = CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
        tag.setTag("kerosene_tank", cap.writeNBT(this.keroseneTank, null));
        tag.setTag("liquid_oxygen_tank", cap.writeNBT(this.liquidOxygenTank, null));

        return tag;
    }

    @Override
    public void deserializeNBT(@Nonnull NBTTagCompound nbt) {
        super.deserializeNBT(nbt);

        Capability<IFluidHandler> cap = CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
        if (nbt.hasKey("kerosene_tank")) {
            cap.readNBT(this.keroseneTank, null, nbt.getCompoundTag("kerosene_tank"));
        }

        if (nbt.hasKey("liquid_oxygen_tank")) {
            cap.readNBT(this.liquidOxygenTank, null, nbt.getCompoundTag("liquid_oxygen_tank"));
        }
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return super.hasCapability(capability, facing) || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || capability == ModuleCapabilities.ADDITIONAL_MASS;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(storage);
        } else if (capability == ModuleCapabilities.ADDITIONAL_MASS) {
            return ModuleCapabilities.ADDITIONAL_MASS.cast(this.storage);
        }
        return super.getCapability(capability, facing);
    }

    private class FuelFluidHandler extends FluidHandlerFluidMap implements IAdditionalMass {
        @Override
        public IFluidTankProperties[] getTankProperties() {
            List<IFluidTankProperties> tanks = Lists.newArrayList();
            for (Map.Entry<Fluid, IFluidHandler> entry : handlers.entrySet()) {
                IFluidTankProperties[] tankProperties = entry.getValue().getTankProperties();
                for (IFluidTankProperties properties : tankProperties) {
                    if (properties.getContents() != null) {
                        tanks.add(properties);
                    } else {
                        tanks.add(new IFluidTankProperties() {
                            @Override
                            public FluidStack getContents() {
                                return new FluidStack(entry.getKey(), 0);
                            }

                            @Override
                            public int getCapacity() {
                                return properties.getCapacity();
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
                            public boolean canFillFluidType(FluidStack fluidStack) {
                                return fluidStack.getFluid() == entry.getKey();
                            }

                            @Override
                            public boolean canDrainFluidType(FluidStack fluidStack) {
                                return fluidStack.getFluid() == entry.getKey();
                            }
                        });
                    }
                }
            }
            return tanks.toArray(new IFluidTankProperties[0]);
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            int fill = super.fill(resource, doFill);
            if (doFill) {
                dirty(true);
            }
            return fill;
        }

        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            FluidStack drain = super.drain(resource, doDrain);
            if (doDrain) {
                dirty(true);
            }
            return drain;
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            FluidStack drain = super.drain(maxDrain, doDrain);
            if (doDrain) {
                dirty(true);
            }
            return drain;
        }

        @Override
        public double getAdditionalMass() {
            double additonalMass = 0.0;
            for (Map.Entry<Fluid, IFluidHandler> handler : handlers.entrySet()) {
                double density = handler.getKey().getDensity() * 0.4;

                IFluidTankProperties properties = handler.getValue().getTankProperties()[0];
                FluidStack contents = properties.getContents();
                if (contents != null) {
                    additonalMass += (double) contents.amount / properties.getCapacity() * density;
                }
            }

            return additonalMass;
        }
    }
}

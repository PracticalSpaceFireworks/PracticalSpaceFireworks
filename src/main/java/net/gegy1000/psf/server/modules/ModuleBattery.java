package net.gegy1000.psf.server.modules;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.IModuleConfig;
import net.gegy1000.psf.api.IModuleConfig.ConfigType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

public class ModuleBattery extends EmptyModule {

    @RequiredArgsConstructor
    public enum BatteryTier implements IStringSerializable {
        SIMPLE("simple", 100_000),
        ADVANCED("advanced", 10_000_000),
        CUSTOM("custom", 0),;

        @Getter
        private final String name;
        private final int capacity;
    }

    private final IEnergyStorage storage;

    public ModuleBattery(BatteryTier tier) {
        this(tier, tier.capacity);
    }

    public ModuleBattery(int capacity) {
        this(BatteryTier.CUSTOM, capacity);
    }

    public ModuleBattery(BatteryTier tier, int capacity) {
        super("battery." + tier.getName());
        this.storage = new EnergyStorage(capacity, capacity, capacity, capacity) {
            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                int ret = super.receiveEnergy(maxReceive, simulate);
                if (ret > 0) {
                    dirty(true);
                }
                return ret;
            }
            
            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                int ret = super.extractEnergy(maxExtract, simulate);
                if (ret > 0) {
                    dirty(true);
                }
                return ret;
            }
        };
    }

    @Override
    public List<String> getSummary() {
        return Arrays.asList(
                "Energy Stored: " + storage.getEnergyStored() + " / " + storage.getMaxEnergyStored()
        );
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = super.serializeNBT();
        tag.setTag("energy", CapabilityEnergy.ENERGY.getStorage().writeNBT(CapabilityEnergy.ENERGY, storage, null));
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        CapabilityEnergy.ENERGY.getStorage().readNBT(CapabilityEnergy.ENERGY, storage, null, nbt.getTag("energy"));
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return super.hasCapability(capability, facing) || capability == CapabilityEnergy.ENERGY;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (hasCapability(capability, facing)) {
            if (capability == CapabilityEnergy.ENERGY) {
                return CapabilityEnergy.ENERGY.cast(storage);
            }
        }
        return super.getCapability(capability, facing);
    }
    
    @Override
    public NBTTagCompound getUpdateTag() {
        return serializeNBT();
    }
    
    @Override
    public void readUpdateTag(NBTTagCompound tag) {
        deserializeNBT(tag);
    }
}

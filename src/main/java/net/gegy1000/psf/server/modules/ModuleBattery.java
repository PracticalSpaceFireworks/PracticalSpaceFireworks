package net.gegy1000.psf.server.modules;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import net.gegy1000.psf.api.IModule;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

public class ModuleBattery extends EmptyModule implements IModule, IEnergyStorage {
    
    @RequiredArgsConstructor
    public enum BatteryTier implements IStringSerializable {
        SIMPLE("simple", 100_000),
        ADVANCED("advanced", 10_000_000),
        CUSTOM("custom", 0),
        ;
        
        @Getter
        private final String name;
        private final int capacity;
    }
    
    @Delegate
    private final IEnergyStorage storage;
    
    public ModuleBattery(BatteryTier tier) {
        this(tier, tier.capacity);
    }

    public ModuleBattery(int capacity) {
        this(BatteryTier.CUSTOM, capacity);
    }
    
    public ModuleBattery(BatteryTier tier, int capacity) { 
        super("battery." + tier.getName());
        this.storage = new EnergyStorage(capacity);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("energy", CapabilityEnergy.ENERGY.getStorage().writeNBT(CapabilityEnergy.ENERGY, this, null));
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        CapabilityEnergy.ENERGY.getStorage().readNBT(CapabilityEnergy.ENERGY, this, null, nbt.getTag("energy"));
    }
}

package net.gegy1000.psf.server.modules;

import net.gegy1000.psf.api.IModule;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;

public class ModuleBattery extends EnergyStorage implements IModule {

    public ModuleBattery(int capacity) {
        super(capacity);
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

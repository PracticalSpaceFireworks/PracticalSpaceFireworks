package net.gegy1000.psf.server.capability.world;

import net.gegy1000.psf.server.satellite.OrbitingSatellite;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public interface SatelliteWorldData extends ICapabilityProvider {
    void addSatellite(OrbitingSatellite satellite);

    void removeSatellite(OrbitingSatellite satellite);

    Collection<OrbitingSatellite> getSatellites();

    class Implementation implements SatelliteWorldData {
        private final Set<OrbitingSatellite> satellites = new HashSet<>();

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
            return capability == CapabilityWorldData.SATELLITE_INSTANCE;
        }

        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
            if (this.hasCapability(capability, facing)) {
                return CapabilityWorldData.SATELLITE_INSTANCE.cast(this);
            }
            return null;
        }

        @Override
        public void addSatellite(OrbitingSatellite satellite) {
            this.satellites.add(satellite);
        }

        @Override
        public void removeSatellite(OrbitingSatellite satellite) {
            this.satellites.remove(satellite);
        }

        @Override
        public Collection<OrbitingSatellite> getSatellites() {
            return Collections.unmodifiableSet(this.satellites);
        }
    }

    class Storage implements Capability.IStorage<SatelliteWorldData> {
        @Override
        public NBTBase writeNBT(Capability<SatelliteWorldData> capability, SatelliteWorldData instance, EnumFacing side) {
            NBTTagCompound compound = new NBTTagCompound();
            NBTTagList satelliteList = new NBTTagList();
            for (OrbitingSatellite satellite : instance.getSatellites()) {
                satelliteList.appendTag(satellite.serialize(new NBTTagCompound()));
            }
            compound.setTag("satellites", satelliteList);
            return compound;
        }

        @Override
        public void readNBT(Capability<SatelliteWorldData> capability, SatelliteWorldData instance, EnumFacing side, NBTBase nbt) {
            NBTTagCompound compound = (NBTTagCompound) nbt;
            NBTTagList satelliteList = compound.getTagList("satellites", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < satelliteList.tagCount(); i++) {
                instance.addSatellite(OrbitingSatellite.deserialize(compound));
            }
        }
    }
}

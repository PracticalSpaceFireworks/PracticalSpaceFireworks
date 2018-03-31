package net.gegy1000.psf.server.capability.world;

import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.satellite.OrbitingSatellite;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public interface SatelliteWorldData extends ICapabilityProvider {
    @Nonnull
    World getWorld();

    void addSatellite(@Nonnull ISatellite satellite);

    void removeSatellite(@Nonnull ISatellite satellite);

    @Nonnull
    Collection<ISatellite> getSatellites();

    class Impl implements SatelliteWorldData {
        private final World world;

        private final Set<ISatellite> satellites = new HashSet<>();

        public Impl(World world) {
            this.world = world;
        }

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
        @Nonnull
        public World getWorld() {
            return this.world;
        }

        @Override
        public void addSatellite(@Nonnull ISatellite satellite) {
            this.satellites.add(satellite);
        }

        @Override
        public void removeSatellite(@Nonnull ISatellite satellite) {
            this.satellites.remove(satellite);
        }

        @Override
        @Nonnull
        public Collection<ISatellite> getSatellites() {
            return Collections.unmodifiableSet(this.satellites);
        }
    }

    class Storage implements Capability.IStorage<SatelliteWorldData> {
        @Override
        public NBTBase writeNBT(Capability<SatelliteWorldData> capability, SatelliteWorldData instance, EnumFacing side) {
            NBTTagCompound compound = new NBTTagCompound();
            NBTTagList satelliteList = new NBTTagList();
            for (ISatellite satellite : instance.getSatellites()) {
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
                instance.addSatellite(OrbitingSatellite.deserialize(instance.getWorld(), compound));
            }
        }
    }
}

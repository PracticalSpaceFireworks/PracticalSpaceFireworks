package net.gegy1000.psf.server.capability.world;

import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.satellite.OrbitingSatellite;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface SatelliteWorldData extends ICapabilitySerializable<NBTTagCompound> {
    @Nonnull
    World getWorld();

    void addSatellite(@Nonnull ISatellite satellite);

    void removeSatellite(@Nonnull UUID id);

    @Nullable
    ISatellite getSatellite(UUID uuid);

    @Nonnull
    Collection<ISatellite> getSatellites();

    class Impl implements SatelliteWorldData {
        private final World world;

        private final Map<UUID, ISatellite> satellites = new HashMap<>();

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
            this.satellites.put(satellite.getId(), satellite);
        }

        @Override
        public void removeSatellite(@Nonnull UUID id) {
            this.satellites.remove(id);
        }

        @Nullable
        @Override
        public ISatellite getSatellite(UUID uuid) {
            return this.satellites.get(uuid);
        }

        @Override
        @Nonnull
        public Collection<ISatellite> getSatellites() {
            return Collections.unmodifiableCollection(this.satellites.values());
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound compound = new NBTTagCompound();
            NBTTagList satelliteList = new NBTTagList();
            for (ISatellite satellite : this.getSatellites()) {
                satelliteList.appendTag(satellite.serializeNBT());
            }
            compound.setTag("satellites", satelliteList);
            return compound;
        }

        @Override
        public void deserializeNBT(NBTTagCompound compound) {
            NBTTagList satelliteList = compound.getTagList("satellites", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < satelliteList.tagCount(); i++) {
                this.addSatellite(OrbitingSatellite.deserialize(this.getWorld(), satelliteList.getCompoundTagAt(i)));
            }
        }
    }
}

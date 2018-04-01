package net.gegy1000.psf.api;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.data.IModuleData;
import net.gegy1000.psf.server.capability.CapabilityModule;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;

@ParametersAreNonnullByDefault
public interface IModule extends INBTSerializable<NBTTagCompound>, ICapabilityProvider {

    default void onSatelliteTick(ISatellite satellite) {
    }

    default int getTickInterval() {
        return 20;
    }

    String getName();

    @SideOnly(Side.CLIENT)
    default String getLocalizedName() {
        return I18n.format(PracticalSpaceFireworks.MODID + ".module." + getName());
    }

    @Nullable
    ResourceLocation getRegistryName();

    IModule setRegistryName(@Nullable ResourceLocation registryName);

    default <T extends IModuleData> Collection<T> getConnectedCaps(ISatellite satellite, Capability<T> capability) {
        return satellite.getModuleCaps(capability);
    }

    @Override
    default boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityModule.INSTANCE;
    }

    @Nullable
    @Override
    default <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (hasCapability(capability, facing)) {
            if (capability == CapabilityModule.INSTANCE) {
                return CapabilityModule.INSTANCE.cast(this);
            }
        }
        return null;
    }
}

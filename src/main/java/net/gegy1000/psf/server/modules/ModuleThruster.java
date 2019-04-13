package net.gegy1000.psf.server.modules;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.gegy1000.psf.api.module.IThruster;
import net.gegy1000.psf.api.module.ModuleCapabilities;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModuleThruster extends EmptyModule {

    @RequiredArgsConstructor
    public enum ThrusterTier implements IStringSerializable, IThruster {
        // TODO: Different names for thrusters
        SIMPLE("simple", 150_000, 125),
        ADVANCED("advanced", 1_260_000, 600);

        @Getter
        private final String name;
        @Getter
        private final double thrust;
        @Getter
        private final int drain;

        @Override
        public double getThrustPerTick() {
            return thrust;
        }

        @Override
        public int getDrainPerTick() {
            return drain;
        }
    }

    @Getter
    private ThrusterTier tier;

    public ModuleThruster(ThrusterTier tier) {
        super("thruster_" + tier.getName());
        this.tier = tier;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = super.serializeNBT();
        tag.setByte("tier", (byte) (this.tier.ordinal() & 0xFF));
        return tag;
    }

    @Override
    public void deserializeNBT(@Nonnull NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        int tier = nbt.getByte("tier") & 0xFF;
        this.tier = ThrusterTier.values()[tier % ThrusterTier.values().length];
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return super.hasCapability(capability, facing) || capability == ModuleCapabilities.THRUSTER;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == ModuleCapabilities.THRUSTER) {
            return ModuleCapabilities.THRUSTER.cast(tier);
        }
        return super.getCapability(capability, facing);
    }
}

package net.gegy1000.psf.server.modules;

import net.gegy1000.psf.api.module.ISeparator;
import net.gegy1000.psf.api.module.ModuleCapabilities;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModuleSeparator extends EmptyModule implements ISeparator {
    public ModuleSeparator(String name) {
        super(name);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return super.hasCapability(capability, facing) || capability == ModuleCapabilities.SEPARATOR;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == ModuleCapabilities.SEPARATOR) {
            return ModuleCapabilities.SEPARATOR.cast(this);
        }
        return super.getCapability(capability, facing);
    }
}

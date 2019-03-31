package net.gegy1000.psf.api;

import javax.annotation.Nonnull;

import net.gegy1000.psf.api.module.IModule;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public final class PSFAPIProps {
    
    private PSFAPIProps() {}
    
    public static final String MODID = "psf";
    
    public static final String VERSION = "0.0.1";
    
    @SuppressWarnings("null")
    @CapabilityInject(IModule.class)
    @Nonnull
    public static final Capability<IModule> CAPABILITY_MODULE = null;
}

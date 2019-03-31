package net.gegy1000.psf.api;

import javax.annotation.Nonnull;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public final class PSFAPIProps {
    
    private PSFAPIProps() {}
    
    public static final String MODID = "psf";
    
    @SuppressWarnings("null")
    @CapabilityInject(IModule.class)
    @Nonnull
    public static final Capability<IModule> CAPABILITY_MODULE = null;
}

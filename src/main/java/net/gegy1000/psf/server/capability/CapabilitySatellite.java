package net.gegy1000.psf.server.capability;

import javax.annotation.Nonnull;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.api.ISatellite;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CapabilitySatellite {

    @SuppressWarnings("null")
    @CapabilityInject(ISatellite.class)
    @Nonnull
    public static final Capability<ISatellite> INSTANCE = null;
    
    public static void register() {
        // TODO default IStorage ?
        CapabilityManager.INSTANCE.register(ISatellite.class, new BlankStorage<>(), () -> null); // FIXME
    }
    
}

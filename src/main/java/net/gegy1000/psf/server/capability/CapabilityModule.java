package net.gegy1000.psf.server.capability;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.api.module.IModule;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nonnull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CapabilityModule {

    @SuppressWarnings("null")
    @CapabilityInject(IModule.class)
    @Nonnull
    public static final Capability<IModule> INSTANCE = null;
    
    public static void register() {
        // TODO default IStorage ?
        CapabilityManager.INSTANCE.register(IModule.class, new BlankStorage<>(), () -> null); // FIXME
    }
    
}

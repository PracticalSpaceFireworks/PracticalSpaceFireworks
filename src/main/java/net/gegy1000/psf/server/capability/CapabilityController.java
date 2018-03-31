package net.gegy1000.psf.server.capability;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.api.IController;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nonnull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CapabilityController {

    @SuppressWarnings("null")
    @CapabilityInject(IController.class)
    @Nonnull
    public static final Capability<IController> INSTANCE = null;
    
    public static void register() {
        // TODO default IStorage ?
        CapabilityManager.INSTANCE.register(IController.class, new BlankStorage<>(), () -> null); // FIXME
    }
    
}

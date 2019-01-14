package net.gegy1000.psf.api;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import lombok.RequiredArgsConstructor;
import net.minecraftforge.registries.IForgeRegistryEntry;

@RequiredArgsConstructor
public class SimpleModuleFactory extends IForgeRegistryEntry.Impl<IModuleFactory> implements IModuleFactory {
    
    private final Supplier<IModule> creator;
    
    @Override
    public @Nonnull IModule get() {
        return creator.get().setRegistryName(getRegistryName());
    }

}

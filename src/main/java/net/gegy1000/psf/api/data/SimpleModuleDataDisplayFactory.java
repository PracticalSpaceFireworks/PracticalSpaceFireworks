package net.gegy1000.psf.api.data;

import javax.annotation.ParametersAreNonnullByDefault;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;
import net.gegy1000.psf.api.ISatellite;
import net.minecraftforge.registries.IForgeRegistryEntry;

@RequiredArgsConstructor
@ParametersAreNonnullByDefault
public class SimpleModuleDataDisplayFactory extends IForgeRegistryEntry.Impl<IModuleDataDisplayFactory> implements IModuleDataDisplayFactory {
    
    private final Supplier<IModuleDataDisplay> defaultFactory;
    private final Function<Iterable<ISatellite>, IModuleDataDisplay> factory;
    
    public SimpleModuleDataDisplayFactory(Supplier<IModuleDataDisplay> defaultFactory) {
        this(defaultFactory, $ -> defaultFactory.get());
    }
    
    @Override
    public Optional<IModuleDataDisplay> create(Iterable<ISatellite> craft) {
        return Optional.ofNullable(factory.apply(craft));
    }

    @Override
    public IModuleDataDisplay createDefault() {
        return defaultFactory.get();
    }
}

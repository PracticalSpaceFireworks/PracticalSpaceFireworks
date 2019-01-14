package net.gegy1000.psf.api.data;

import javax.annotation.ParametersAreNonnullByDefault;

import java.util.Optional;

import net.gegy1000.psf.api.ISatellite;
import net.minecraftforge.registries.IForgeRegistryEntry;

@ParametersAreNonnullByDefault
public interface IModuleDataDisplayFactory extends IForgeRegistryEntry<IModuleDataDisplayFactory> {
    
    /**
     * Optionally create a display for the given {@link ISatellite craft}. If no relevant data can be displayed for the given craft, return {@link Optional#empty()}.
     */
    default Optional<IModuleDataDisplay> create(ISatellite craft) {
        return Optional.of(createDefault());
    }

    /**
     * Create a default to be filled in by NBT deserialization.
     * If no {@link #create(ISatellite)} implementation is given, this is used for initial creation as well.
     */
    IModuleDataDisplay createDefault();
}

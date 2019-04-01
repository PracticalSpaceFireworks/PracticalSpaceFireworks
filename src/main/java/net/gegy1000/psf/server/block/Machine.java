package net.gegy1000.psf.server.block;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface Machine {
    // TODO Setup machines to toggle this for actual active state in TE's
    // Do we need to serialize this? It could probably be inferred from the TE
    PropertyBool ACTIVE = PropertyBool.create("active");

    default boolean isIdle(IBlockState state) {
        return !isActive(state);
    }

    default boolean isActive(IBlockState state) {
        return state.getValue(ACTIVE);
    }
}

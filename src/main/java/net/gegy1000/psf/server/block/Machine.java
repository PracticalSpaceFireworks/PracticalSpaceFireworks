package net.gegy1000.psf.server.block;

import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface Machine {
    PropertyBool ACTIVE = PropertyBool.create("active");

    static boolean isIdle(IBlockState state) {
        return !isActive(state);
    }

    static boolean isActive(IBlockState state) {
        return state.getValue(ACTIVE);
    }
}

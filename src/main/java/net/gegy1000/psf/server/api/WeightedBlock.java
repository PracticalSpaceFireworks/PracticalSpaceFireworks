package net.gegy1000.psf.server.api;

import net.minecraft.block.state.IBlockState;

public interface WeightedBlock {
    double getMass(IBlockState state);
}

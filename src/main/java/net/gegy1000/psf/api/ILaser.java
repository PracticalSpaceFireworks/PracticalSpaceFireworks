package net.gegy1000.psf.api;

import net.minecraft.util.math.BlockPos;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface ILaser {

    boolean isActive();
    
    boolean activate(ISatellite craft, BlockPos target);
    
}

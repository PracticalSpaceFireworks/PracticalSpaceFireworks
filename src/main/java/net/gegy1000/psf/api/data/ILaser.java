package net.gegy1000.psf.api.data;

import javax.annotation.ParametersAreNonnullByDefault;

import net.gegy1000.psf.api.ISatellite;
import net.minecraft.util.math.BlockPos;

@ParametersAreNonnullByDefault
public interface ILaser {

    boolean isActive();
    
    void activate(ISatellite craft, BlockPos target);
    
}

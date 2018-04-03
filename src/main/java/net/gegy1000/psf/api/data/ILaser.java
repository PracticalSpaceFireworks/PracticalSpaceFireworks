package net.gegy1000.psf.api.data;

import net.gegy1000.psf.api.ISatellite;
import net.minecraft.util.math.BlockPos;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface ILaser {

    boolean isActive();
    
    boolean activate(ISatellite craft, BlockPos target);
    
}

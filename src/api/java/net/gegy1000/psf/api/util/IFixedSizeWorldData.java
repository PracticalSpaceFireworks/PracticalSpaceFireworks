package net.gegy1000.psf.api.util;

import net.minecraft.util.math.BlockPos;

public interface IFixedSizeWorldData extends IWorldData {
    
    BlockPos getMinPos();
    
    BlockPos getMaxPos();

}

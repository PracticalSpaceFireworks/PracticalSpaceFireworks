package net.gegy1000.psf.api;

import java.util.Optional;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.util.math.BlockPos;

@ParametersAreNonnullByDefault
public interface IController extends IModule {
    
    Optional<BlockPos> getPosition();

}

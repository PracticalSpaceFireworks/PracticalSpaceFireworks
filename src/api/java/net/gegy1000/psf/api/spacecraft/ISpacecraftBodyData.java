package net.gegy1000.psf.api.spacecraft;

import net.gegy1000.psf.api.module.IModule;
import net.gegy1000.psf.api.util.IFixedSizeWorldData;

import javax.annotation.Nonnull;
import java.util.List;

public interface ISpacecraftBodyData extends IFixedSizeWorldData {

    @Nonnull
    List<IModule> collectModules();

}

package net.gegy1000.psf.api.spacecraft;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;

import net.gegy1000.psf.api.module.IModule;
import net.gegy1000.psf.api.util.IFixedSizeWorldData;
import net.minecraft.world.World;

public interface ISpacecraftBodyData extends IFixedSizeWorldData {

    @Nullable
    IController findController();

    @Nonnull
    List<IModule> findModules();

    ISpacecraftMetadata buildSpacecraftMetadata(World parent);

}

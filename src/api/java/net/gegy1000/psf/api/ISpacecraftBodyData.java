package net.gegy1000.psf.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;

import net.minecraft.world.World;

public interface ISpacecraftBodyData extends IFixedSizeWorldData {

    @Nullable
    IController findController();

    @Nonnull
    List<IModule> findModules();

    ISpacecraftMetadata buildSpacecraftMetadata(World parent);

}

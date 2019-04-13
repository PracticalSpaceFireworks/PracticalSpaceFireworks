package net.gegy1000.psf.api.spacecraft;

import net.gegy1000.psf.api.util.IUnique;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
public interface IListedSpacecraft extends IUnique, IStringSerializable {

    void setName(@Nonnull String name);

    @Override
    @Nonnull
    String getName();

    @Nonnull
    BlockPos getPosition();

    default boolean isOrbiting() {
        return false;
    }

    default Optional<LaunchHandle> getLaunchHandle() {
        return Optional.empty();
    }

    boolean isDestroyed();

    interface LaunchHandle {
        void launch();
    }
}

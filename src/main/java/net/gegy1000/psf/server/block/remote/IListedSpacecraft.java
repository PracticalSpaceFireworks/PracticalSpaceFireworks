package net.gegy1000.psf.server.block.remote;

import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.IUnique;
import net.gegy1000.psf.client.IVisualReceiver;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftWorldHandler;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;

@ParametersAreNonnullByDefault
public interface IListedSpacecraft extends IUnique, IStringSerializable {
    
    @Override
    @Nonnull
    String getName();

    void setName(@Nonnull String name);

    @Nonnull
    BlockPos getPosition();

    default boolean isOrbiting() {
        return false;
    }

    default boolean canLaunch() {
        return false;
    }

    default void launch() {
        throw new UnsupportedOperationException();
    }

    class Visual implements IVisualReceiver.IVisual {
        private final SpacecraftWorldHandler worldHandler;
        private final Collection<IModule> modules;

        public Visual(SpacecraftWorldHandler worldHandler, Collection<IModule> modules) {
            this.worldHandler = worldHandler;
            this.modules = modules;
        }

        @Nonnull
        @Override
        public SpacecraftWorldHandler getWorldHandler() {
            return this.worldHandler;
        }

        @Nonnull
        @Override
        public Collection<IModule> getModules() {
            return this.modules;
        }
    }
}

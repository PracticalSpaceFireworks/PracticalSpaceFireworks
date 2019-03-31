package net.gegy1000.psf.api.spacecraft;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import java.util.Collection;

import net.gegy1000.psf.api.client.IVisualReceiver;
import net.gegy1000.psf.api.module.IModule;
import net.gegy1000.psf.api.util.IUnique;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;

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
    
    boolean isDestroyed();

    class Visual implements IVisualReceiver.IVisual {
        private final ISpacecraftBodyData bodyData;
        private final Collection<IModule> modules;

        public Visual(ISpacecraftBodyData bodyData, Collection<IModule> modules) {
            this.bodyData = bodyData;
            this.modules = modules;
        }

        @Nonnull
        @Override
        public Collection<IModule> getModules() {
            return this.modules;
        }

        @Override
        public ISpacecraftBodyData getBodyData() {
            return bodyData;
        }
    }
}

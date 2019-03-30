package net.gegy1000.psf.server.block.remote;

import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.IUnique;
import net.gegy1000.psf.client.IVisualReceiver;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftBodyData;
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
    
    boolean isDestroyed();

    class Visual implements IVisualReceiver.IVisual {
        private final SpacecraftBodyData bodyData;
        private final Collection<IModule> modules;

        public Visual(SpacecraftBodyData bodyData, Collection<IModule> modules) {
            this.bodyData = bodyData;
            this.modules = modules;
        }

        @Nonnull
        @Override
        public Collection<IModule> getModules() {
            return this.modules;
        }

        @Override
        public SpacecraftBodyData getBodyData() {
            return bodyData;
        }
    }
}

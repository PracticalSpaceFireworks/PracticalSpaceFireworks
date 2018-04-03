package net.gegy1000.psf.client;

import java.util.Collection;
import java.util.UUID;

import javax.annotation.ParametersAreNonnullByDefault;

import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.server.block.remote.IListedSpacecraft;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftBlockAccess;
import net.minecraft.nbt.NBTTagCompound;

@ParametersAreNonnullByDefault
public interface IVisualReceiver {
    
    public interface IVisual {

        public SpacecraftBlockAccess getBlockAccess();

        public Collection<IModule> getModules();
    }
    
    void setVisual(IVisual visual);
    
    void updateCraft(IListedSpacecraft craft);

    void updateModule(UUID id, NBTTagCompound tag);
}

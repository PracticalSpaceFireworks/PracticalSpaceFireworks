package net.gegy1000.psf.client;

import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.server.block.remote.IListedSpacecraft;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftWorldHandler;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.UUID;

@ParametersAreNonnullByDefault
public interface IVisualReceiver {
    
    interface IVisual {

        SpacecraftWorldHandler getWorldHandler();

        Collection<IModule> getModules();
    }
    
    void setVisual(IVisual visual);
    
    void updateCraft(IListedSpacecraft craft);

    void updateModule(UUID id, NBTTagCompound tag);
}

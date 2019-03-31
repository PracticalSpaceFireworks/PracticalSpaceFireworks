package net.gegy1000.psf.api;

import net.gegy1000.psf.api.IModule;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.UUID;

@ParametersAreNonnullByDefault
public interface IVisualReceiver {
    
    interface IVisual {

        Collection<IModule> getModules();

        ISpacecraftBodyData getBodyData();
    }
    
    void setVisual(IVisual visual);
    
    void updateCraft(IListedSpacecraft craft);
    
    void removeCraft(UUID id);

    void updateModule(UUID id, NBTTagCompound tag);
}

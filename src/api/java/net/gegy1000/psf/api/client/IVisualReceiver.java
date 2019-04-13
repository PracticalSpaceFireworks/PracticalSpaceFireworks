package net.gegy1000.psf.api.client;

import net.gegy1000.psf.api.spacecraft.IListedSpacecraft;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@ParametersAreNonnullByDefault
public interface IVisualReceiver {

    void setVisual(IVisualData visual);

    void updateCraft(IListedSpacecraft craft);

    void removeCraft(UUID id);

    void updateModule(UUID id, NBTTagCompound tag);
}

package net.gegy1000.psf.api.data;

import javax.annotation.ParametersAreNonnullByDefault;

import net.gegy1000.psf.api.ISatellite;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.registries.IForgeRegistryEntry;

@ParametersAreNonnullByDefault
public interface IModuleDataDisplayFactory extends IForgeRegistryEntry<IModuleDataDisplayFactory> {

    /**
     * Create a default to be filled in by NBT deserialization.
     * If no {@link #create(ISatellite)} implementation is given, this is used for initial creation as well.
     */
    IModuleDataDisplay create();
    
    /**
     * Convert client data into an update packet to send back to the client. Data is fed from the client via {@link IModuleDataDisplay#getRequestData()}.
     */
    default NBTTagCompound getUpdateData(NBTTagCompound requestData) { return null; }
}

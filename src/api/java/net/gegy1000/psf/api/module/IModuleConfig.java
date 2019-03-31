package net.gegy1000.psf.api.module;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

@ParametersAreNonnullByDefault
public interface IModuleConfig extends INBTSerializable<NBTTagCompound> {
    
    enum ConfigType {
        ACTION,
        TOGGLE,
        TEXT
    }
    
    String getKey();
    
    /**
     * @return The current value represented as a string
     */
    String getValue();
    
    /**
     * Called when the value is changed on the client. Use this method to update your internal state for syncing to the server.
     * @param newValue An object (generally a string, or a number for slider) representing the new value, if applicable.
     */
    default void modified(@Nullable Object newValue) {}
    
    /**
     * Called after deserializing this config on the server. Use this to perform any actions based on the new state.
     */
    default void modified() {}

    ConfigType getType();

    @Override
    void deserializeNBT(@Nullable NBTTagCompound tag);

}

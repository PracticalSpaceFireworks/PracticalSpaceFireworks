package net.gegy1000.psf.api;

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
     * Called when the value is changed.
     * @param newValue An object (generally a string, or a number for slider) representing the new value, if applicable.
     */
    default void modified(@Nullable Object newValue) {}

    ConfigType getType();

    /**
     * Only called on the server, when the value has changed clientside.
     * <p>
     * This should probably call {@link #modified(Object)} in your implementation.
     */
    @Override
    void deserializeNBT(@Nullable NBTTagCompound tag);

}

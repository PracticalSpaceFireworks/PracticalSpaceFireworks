package net.gegy1000.psf.api.data;

import net.minecraft.nbt.NBTTagCompound;

public interface IModuleDataDisplay {
    
    void draw(int x, int y, int width, int height, float partialTicks);
    
    default void mouseMove(int x, int y, boolean drag) {}
    
    default void mouseClick(int x, int y, int button, boolean down) {}
    
    default boolean needsUpdate() { return false; }

    /**
     * The information to send to {@link IModuleDataDisplayFactory#getUpdateData(NBTTagCompound)} to request a data update.
     */
    default NBTTagCompound getRequestData() { return null; }
    
    /**
     * Update this display with the server data returned from {@link IModuleDataDisplayFactory#getUpdateData(NBTTagCompound)}.
     */
    default void updateData(NBTTagCompound data) {}
}

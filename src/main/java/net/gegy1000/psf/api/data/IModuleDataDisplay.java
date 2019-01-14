package net.gegy1000.psf.api.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

public interface IModuleDataDisplay extends INBTSerializable<NBTTagCompound> {
    
    void draw(int x, int y, int width, int height, float partialTicks);

}

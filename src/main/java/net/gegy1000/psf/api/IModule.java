package net.gegy1000.psf.api;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface IModule extends INBTSerializable<NBTTagCompound> {
    
    default void onCraftTick(EntitySpacecraft craft) {}
        
    String getName();
    
    @SideOnly(Side.CLIENT)
    default String getLocalizedName() {
        return I18n.format(PracticalSpaceFireworks.MODID + ".module." + getName());
    }
    
    @Nullable
    ResourceLocation getRegistryName();

    IModule setRegistryName(@Nullable ResourceLocation registryName);
}

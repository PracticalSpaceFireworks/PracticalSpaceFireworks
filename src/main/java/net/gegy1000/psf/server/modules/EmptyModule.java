package net.gegy1000.psf.server.modules;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.gegy1000.psf.api.IModule;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

@RequiredArgsConstructor
public class EmptyModule implements IModule {
    
    @Getter
    @Setter
    @Accessors(chain = true)
    private ResourceLocation registryName;
    
    @Getter
    private final String name;
    
    @Override
    public NBTTagCompound serializeNBT() {
        return new NBTTagCompound();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {}
}

package net.gegy1000.psf.server.modules;

import java.util.UUID;

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
    private UUID id = UUID.randomUUID();
    
    @Getter
    @Setter
    @Accessors(chain = true)
    private ResourceLocation registryName;
    
    @Getter
    private final String name;
    
    @Getter
    private boolean dirty = true;
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound ret = new NBTTagCompound();
        ret.setLong("id_msb", id.getMostSignificantBits());
        ret.setLong("id_lsb", id.getLeastSignificantBits());
        return ret;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("id_msb")) {
            this.id = new UUID(nbt.getLong("id_msb"), nbt.getLong("id_lsb"));
        }
    }
    
    @Override
    public void dirty(boolean dirty) {
        this.dirty = dirty;
    }
}

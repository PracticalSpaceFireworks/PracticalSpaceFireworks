package net.gegy1000.psf.server.modules;

import java.util.UUID;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.IModule;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
    
    @SideOnly(Side.CLIENT)
    @Override
    public String getLocalizedName() {
        return I18n.format(String.format("tile.%s.%s.name", PracticalSpaceFireworks.MODID, getName()));
    }
    
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

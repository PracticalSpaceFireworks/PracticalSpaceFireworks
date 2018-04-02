package net.gegy1000.psf.server.modules;

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

import java.util.UUID;

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
        ret.setUniqueId("id", id);
        return ret;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("idMost")) {
            this.id = nbt.getUniqueId("id");
        }
    }
    
    @Override
    public void dirty(boolean dirty) {
        this.dirty = dirty;
    }
}

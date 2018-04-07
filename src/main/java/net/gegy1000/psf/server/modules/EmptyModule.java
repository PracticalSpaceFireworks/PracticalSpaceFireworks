package net.gegy1000.psf.server.modules;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.IModuleConfig;
import net.gegy1000.psf.api.ISatellite;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@ParametersAreNonnullByDefault
public class EmptyModule implements IModule {
    
    private final Map<String, IModuleConfig> configs = new HashMap<>();
    
    @Getter
    private UUID id = UUID.randomUUID();
    
    @Getter
    @Setter
    @Accessors(chain = true)
    private ResourceLocation registryName;
    
    @Nonnull
    @Getter
    private final String name;
    
    @Getter
    private boolean dirty = true;

    @Nullable
    @Getter
    @Setter
    private ISatellite owner;

    @SideOnly(Side.CLIENT)
    @Override
    public @Nonnull String getLocalizedName() {
        return I18n.format(String.format("tile.%s.%s.name", PracticalSpaceFireworks.MODID, getName()));
    }
    
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound ret = new NBTTagCompound();
        ret.setUniqueId("id", id);
        NBTTagCompound configTag = new NBTTagCompound();
        for (val e : configs.entrySet()) {
            configTag.setTag(e.getKey(), e.getValue().serializeNBT());
        }
        ret.setTag("configs", configTag);
        return ret;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("idMost")) {
            this.id = nbt.getUniqueId("id");
        }
        NBTTagCompound configTag = nbt.getCompoundTag("configs");
        for (val e : configs.entrySet()) {
            IModuleConfig cfg = getConfig(e.getKey());
            if (cfg != null) {
                cfg.deserializeNBT(configTag.getCompoundTag(e.getKey()));
            }
        }
    }
    
    @Override
    public void dirty(boolean dirty) {
        this.dirty = dirty;
    }
    
    protected final void registerConfigs(IModuleConfig... cfgs) {
        for (IModuleConfig cfg : cfgs) {
            this.configs.put(cfg.getKey(), cfg);
        }
    }
    
    @Override
    @Nullable
    public IModuleConfig getConfig(String key) {
        return this.configs.get(key);
    }
    
    @Override
    public @Nonnull Collection<IModuleConfig> getConfigs() {
        return this.configs.values();
    }
    
    @Override
    public String toString() {
        return getName();
    }
}

package net.gegy1000.psf.server.modules.configs;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.nbt.NBTTagCompound;

@ParametersAreNonnullByDefault
public class ConfigBasicAction extends AbstractConfig {
    
    private final String content;

    public ConfigBasicAction(String key, String content) {
        super(key);
        this.content = content;
    }

    @Override
    public String getValue() {
        return content;
    }

    @Override
    public ConfigType getType() {
        return ConfigType.ACTION;
    }

    @Override
    public void deserializeNBT(@Nullable NBTTagCompound tag) {}

    @Override
    public NBTTagCompound serializeNBT() { return new NBTTagCompound(); }

}

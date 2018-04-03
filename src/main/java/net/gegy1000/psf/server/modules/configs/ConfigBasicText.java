package net.gegy1000.psf.server.modules.configs;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.Strings;

import net.minecraft.nbt.NBTTagCompound;

@ParametersAreNonnullByDefault
public class ConfigBasicText extends AbstractConfig {
    
    private String text;

    protected ConfigBasicText(String key, String defaultText) {
        super(key);
        this.text = defaultText;
    }

    @Override
    public String getValue() {
        return text;
    }

    @Override
    public ConfigType getType() {
        return ConfigType.TEXT;
    }
    
    @Override
    public void modified(@Nullable Object newValue) {
        this.text = Strings.nullToEmpty((String) newValue);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound ret = new NBTTagCompound();
        ret.setString("text", text);
        return ret;
    }

    @Override
    public void deserializeNBT(@Nullable NBTTagCompound tag) {
        if (tag != null) {
            this.text = tag.getString("text");
        }
    }
}

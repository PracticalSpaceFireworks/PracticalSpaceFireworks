package net.gegy1000.psf.server.modules.configs;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.gegy1000.psf.api.IModuleConfigDisplay;
import net.gegy1000.psf.server.block.remote.config.ModuleConfigButtonAction;
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
    public IModuleConfigDisplay getDisplay() {
        return ModuleConfigButtonAction.factory(this);
    }

    @Override
    public void deserializeNBT(@Nullable NBTTagCompound tag) {}

    @Override
    public NBTTagCompound serializeNBT() { return new NBTTagCompound(); }

}

package net.gegy1000.psf.server.modules.configs;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ConfigBooleanToggle extends ConfigBasicToggle {

    public ConfigBooleanToggle(String key, String trueStr, String falseStr) {
        super(key, falseStr, trueStr); // arguments passed BACKWARDS!!
    }

    public boolean value() {
        return getState() == 1;
    }
}

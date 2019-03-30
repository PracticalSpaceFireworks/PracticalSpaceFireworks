package net.gegy1000.psf.server.modules.configs;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ConfigBooleanToggle extends ConfigBasicToggle {

    public ConfigBooleanToggle(String key, boolean defaultState, String trueStr, String falseStr) {
        super(key, defaultState ? 1 : 0, falseStr, trueStr); // arguments passed BACKWARDS!!
    }

    public boolean value() {
        return getState() == 1;
    }
}

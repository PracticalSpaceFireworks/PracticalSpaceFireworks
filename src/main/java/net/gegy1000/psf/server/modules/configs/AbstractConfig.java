package net.gegy1000.psf.server.modules.configs;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.gegy1000.psf.api.IModuleConfig;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractConfig implements IModuleConfig {
    
    @Getter
    private final String key;

}

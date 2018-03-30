package net.gegy1000.psf.server.block.module;

import javax.annotation.ParametersAreNonnullByDefault;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.IStringSerializable;

@ParametersAreNonnullByDefault
@RequiredArgsConstructor
public enum StrutType implements IStringSerializable {
    
    CUBE("cube"),
    ;
    
    @Getter
    private final String name;
}

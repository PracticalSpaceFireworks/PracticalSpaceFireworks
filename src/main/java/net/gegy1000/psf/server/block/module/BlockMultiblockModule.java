package net.gegy1000.psf.server.block.module;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;

import net.minecraft.block.properties.PropertyBool;

public class BlockMultiblockModule extends BlockModule {
    
    public BlockMultiblockModule(Material mat, String module) {
        super(mat, module);
    }

    public static final IProperty<Boolean> DUMMY = PropertyBool.create("dummy");
    
    

}

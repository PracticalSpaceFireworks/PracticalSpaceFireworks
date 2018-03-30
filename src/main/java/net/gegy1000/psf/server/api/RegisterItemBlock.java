package net.gegy1000.psf.server.api;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

@ParametersAreNonnullByDefault
public interface RegisterItemBlock {
    
    default ItemBlock createItemBlock(Block block) {
        return new ItemBlock(block);
    }
}

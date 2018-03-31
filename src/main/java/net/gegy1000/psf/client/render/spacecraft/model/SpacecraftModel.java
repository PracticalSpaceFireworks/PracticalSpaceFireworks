package net.gegy1000.psf.client.render.spacecraft.model;

import net.gegy1000.psf.server.entity.spacecraft.SpacecraftBlockAccess;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface SpacecraftModel {
    @SideOnly(Side.CLIENT)
    static SpacecraftModel build(SpacecraftBlockAccess blockAccess) {
        // TODO: Implement displaylist based model
//        if (Minecraft.getMinecraft().gameSettings.useVbo) {
//
//        } else {
//
//        }
        return new VboSpacecraftModel(blockAccess);
    }

    void render(BlockRenderLayer layer);

    void delete();

    boolean isAvailable();
    
    SpacecraftBlockAccess getRenderWorld();
}

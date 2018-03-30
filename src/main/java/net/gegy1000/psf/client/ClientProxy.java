package net.gegy1000.psf.client;

import net.gegy1000.psf.client.render.spacecraft.RenderSpacecraft;
import net.gegy1000.psf.server.ServerProxy;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends ServerProxy {
    @Override
    public void onPreInit() {
        super.onPreInit();

        RenderingRegistry.registerEntityRenderingHandler(EntitySpacecraft.class, RenderSpacecraft::new);
    }

    @Override
    public void onInit() {
        super.onInit();
    }

    @Override
    public void onPostInit() {
        super.onPostInit();
    }
}

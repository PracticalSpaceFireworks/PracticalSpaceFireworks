package net.gegy1000.psf.client;

import net.gegy1000.psf.client.render.spacecraft.RenderSpacecraft;
import net.gegy1000.psf.server.ServerProxy;
import net.gegy1000.psf.server.block.controller.ControllerManager;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;

@SideOnly(Side.CLIENT)
public class ClientProxy extends ServerProxy {

    private final ControllerManager controllerManager = new ControllerManager();

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
    
    @Override
    public ControllerManager getControllerManager(boolean remote) {
        if (remote) {
            return controllerManager;
        } else {
            return super.getControllerManager(remote);
        }
    }

    @Override
    public void handlePacket(MessageContext context, Consumer<EntityPlayer> handle) {
        if (context.side == Side.SERVER) {
            super.handlePacket(context, handle);
        } else {
            Minecraft.getMinecraft().addScheduledTask(() -> handle.accept(Minecraft.getMinecraft().player));
        }
    }
}

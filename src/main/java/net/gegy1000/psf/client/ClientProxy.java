package net.gegy1000.psf.client;

import java.util.function.Consumer;

import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.client.render.spacecraft.RenderSpacecraft;
import net.gegy1000.psf.server.ServerProxy;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.gegy1000.psf.server.satellite.UniqueManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends ServerProxy {

    private final UniqueManager<ISatellite> controllerManager = new UniqueManager<>();

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
    public UniqueManager<ISatellite> getSatellites() {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            throw new IllegalStateException("Cannot get satellite cache on client");
        }
        return controllerManager;
    }

    @Override
    public void handlePacket(MessageContext context, Consumer<EntityPlayer> handle) {
        if (context.side == Side.SERVER) {
            super.handlePacket(context, handle);
        } else {
            Minecraft.getMinecraft().addScheduledTask(() -> handle.accept(Minecraft.getMinecraft().player));
        }
    }
    
    @Override
    public World getClientWorld() {
        return Minecraft.getMinecraft().world;
    }
}

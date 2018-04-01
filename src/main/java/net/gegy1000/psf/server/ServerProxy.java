package net.gegy1000.psf.server;

import net.gegy1000.psf.server.block.controller.ControllerManager;
import net.gegy1000.psf.server.capability.CapabilityController;
import net.gegy1000.psf.server.capability.CapabilityModule;
import net.gegy1000.psf.server.capability.CapabilityModuleData;
import net.gegy1000.psf.server.capability.CapabilitySatellite;
import net.gegy1000.psf.server.capability.world.CapabilityWorldData;
import net.gegy1000.psf.server.util.BlockMassHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.function.Consumer;

public class ServerProxy {
    
    private final ControllerManager controllerManager = new ControllerManager();
    
    public void onPreInit() {
        BlockMassHandler.register();

        CapabilityWorldData.register();
        CapabilityController.register();
        CapabilityModuleData.register();
        CapabilityModule.register();
        CapabilitySatellite.register();
    }

    public void onInit() {
    }

    public void onPostInit() {
    }
    
    public ControllerManager getControllerManager(boolean remote) {
        if (remote) {
            throw new IllegalArgumentException("Cannot retrieve client manager from dedicated server!");
        }
        return controllerManager;
    }

    public void handlePacket(MessageContext context, Consumer<EntityPlayer> handle) {
        FMLCommonHandler.instance().getWorldThread(context.netHandler).addScheduledTask(() -> handle.accept(context.getServerHandler().player));
    }
}

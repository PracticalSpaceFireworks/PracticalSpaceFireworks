package net.gegy1000.psf.server;

import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.capability.CapabilityController;
import net.gegy1000.psf.server.capability.CapabilityModule;
import net.gegy1000.psf.server.capability.CapabilityModuleData;
import net.gegy1000.psf.server.capability.CapabilitySatellite;
import net.gegy1000.psf.server.capability.world.CapabilityWorldData;
import net.gegy1000.psf.server.fluid.PSFFluidRegistry;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.gegy1000.psf.server.satellite.UniqueManager;
import net.gegy1000.psf.server.util.BlockMassHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.function.Consumer;

public class ServerProxy {
    
    private final UniqueManager<ISatellite> satelliteManager = new UniqueManager<>();
    
    public void onPreInit() {
        BlockMassHandler.register();
        PSFFluidRegistry.register();

        CapabilityWorldData.register();
        CapabilityController.register();
        CapabilityModuleData.register();
        CapabilityModule.register();
        CapabilitySatellite.register();
    }

    public void onInit() {
        PSFNetworkHandler.register();
    }

    public void onPostInit() {
    }
    
    public UniqueManager<ISatellite> getSatellites() {
        return satelliteManager;
    }

    public void handlePacket(MessageContext context, Consumer<EntityPlayer> handle) {
        FMLCommonHandler.instance().getWorldThread(context.netHandler).addScheduledTask(() -> handle.accept(context.getServerHandler().player));
    }

    public World getClientWorld() {
        return null;
    }
}

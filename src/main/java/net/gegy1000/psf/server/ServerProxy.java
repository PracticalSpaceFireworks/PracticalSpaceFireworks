package net.gegy1000.psf.server;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.spacecraft.ISatellite;
import net.gegy1000.psf.server.capability.CapabilityController;
import net.gegy1000.psf.server.capability.CapabilityModule;
import net.gegy1000.psf.server.capability.CapabilityModuleData;
import net.gegy1000.psf.server.capability.CapabilitySatellite;
import net.gegy1000.psf.server.capability.world.CapabilityWorldData;
import net.gegy1000.psf.server.init.PSFFluids;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.gegy1000.psf.server.satellite.UniqueManager;
import net.gegy1000.psf.server.util.BlockMassHandler;
import net.gegy1000.psf.server.util.PSFGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.function.Consumer;

public class ServerProxy {
    
    private final UniqueManager<ISatellite> satelliteManager = new UniqueManager<ISatellite>()
            .onRemove(ISatellite::onRemove);
    
    public void onPreInit() {
        BlockMassHandler.register();
        PSFFluids.init();

        CapabilityWorldData.register();
        CapabilityController.register();
        CapabilityModuleData.register();
        CapabilityModule.register();
        CapabilitySatellite.register();

        NetworkRegistry.INSTANCE.registerGuiHandler(PracticalSpaceFireworks.MODID, new PSFGuiHandler());
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
}

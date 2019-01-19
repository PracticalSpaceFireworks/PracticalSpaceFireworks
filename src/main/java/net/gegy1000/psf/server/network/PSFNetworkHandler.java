package net.gegy1000.psf.server.network;

import com.google.common.reflect.Reflection;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.block.data.packet.PacketDisplayUpdate;
import net.gegy1000.psf.server.block.data.packet.PacketRequestDisplayUpdate;
import net.gegy1000.psf.server.block.remote.config.PacketConfigChange;
import net.gegy1000.psf.server.block.remote.packet.PacketCraftState;
import net.gegy1000.psf.server.block.remote.packet.PacketOpenRemoteControl;
import net.gegy1000.psf.server.block.remote.packet.PacketRequestVisual;
import net.gegy1000.psf.server.block.remote.packet.PacketSetName;
import net.gegy1000.psf.server.block.remote.packet.PacketTrackCraft;
import net.gegy1000.psf.server.block.remote.packet.PacketVisualData;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.gegy1000.psf.server.modules.data.PacketLaserState;
import net.gegy1000.psf.server.satellite.PacketModule;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PSFNetworkHandler {
    
    public static final SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel(PracticalSpaceFireworks.MODID);

    private static int ID = 0;
    
    public static int nextID() {
        return ID++;
    }
    
    public static void register() {
        /* Control System Data */
        network.registerMessage(PacketTrackCraft.Handler.class, PacketTrackCraft.class, nextID(), Side.SERVER);
        network.registerMessage(PacketVisualData.Handler.class, PacketVisualData.class, nextID(), Side.CLIENT);
        network.registerMessage(PacketOpenRemoteControl.Handler.class, PacketOpenRemoteControl.class, nextID(), Side.CLIENT);
        network.registerMessage(PacketCraftState.Handler.class, PacketCraftState.class, nextID(), Side.CLIENT);
        network.registerMessage(PacketRequestVisual.Handler.class, PacketRequestVisual.class, nextID(), Side.SERVER);
        network.registerMessage(PacketSetName.Handler.class, PacketSetName.class, nextID(), Side.SERVER);
        network.registerMessage(PacketConfigChange.Handler.class, PacketConfigChange.class, nextID(), Side.SERVER);
        
        /* Data Viewer */
        network.registerMessage(PacketRequestDisplayUpdate.Handler.class, PacketRequestDisplayUpdate.class, nextID(), Side.SERVER);
        network.registerMessage(PacketDisplayUpdate.Handler.class, PacketDisplayUpdate.class, nextID(), Side.CLIENT);

        /* Module Syncing */
        network.registerMessage(PacketModule.Handler.class, PacketModule.class, nextID(), Side.CLIENT);
        network.registerMessage(PacketLaserState.Handler.class, PacketLaserState.class, nextID(), Side.CLIENT);

        Reflection.initialize(EntitySpacecraft.class);
    }
}

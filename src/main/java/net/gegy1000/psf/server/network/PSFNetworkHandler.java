package net.gegy1000.psf.server.network;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.block.remote.packet.PacketOpenRemoteControl;
import net.gegy1000.psf.server.block.remote.packet.PacketRequestVisual;
import net.gegy1000.psf.server.block.remote.packet.PacketSetName;
import net.gegy1000.psf.server.block.remote.packet.PacketTrackCraft;
import net.gegy1000.psf.server.block.remote.packet.PacketVisualData;
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
        network.registerMessage(PacketRequestVisual.Handler.class, PacketRequestVisual.class, nextID(), Side.SERVER);
        network.registerMessage(PacketSetName.Handler.class, PacketSetName.class, nextID(), Side.SERVER);
    
        /* Module Syncing */
        network.registerMessage(PacketModule.Handler.class, PacketModule.class, nextID(), Side.CLIENT);
    }
}

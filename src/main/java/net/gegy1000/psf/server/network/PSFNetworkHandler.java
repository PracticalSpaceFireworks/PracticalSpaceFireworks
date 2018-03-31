package net.gegy1000.psf.server.network;

import net.gegy1000.psf.PracticalSpaceFireworks;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class PSFNetworkHandler {
    
    public static final SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel(PracticalSpaceFireworks.MODID);

    private static int ID = 0;
    
    public static int nextID() {
        return ID++;
    }
}

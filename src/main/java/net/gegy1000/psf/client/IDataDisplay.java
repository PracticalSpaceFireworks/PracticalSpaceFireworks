package net.gegy1000.psf.client;

import net.gegy1000.psf.api.data.IModuleDataDisplay;
import net.gegy1000.psf.api.data.IModuleDataDisplayFactory;
import net.gegy1000.psf.server.block.data.packet.PacketRequestDisplay;
import net.gegy1000.psf.server.network.PSFNetworkHandler;

public interface IDataDisplay {
    
    default void requestDisplay(IModuleDataDisplayFactory factory) {
        PSFNetworkHandler.network.sendToServer(new PacketRequestDisplay(factory));
    }

    void setDisplay(IModuleDataDisplay display);
}

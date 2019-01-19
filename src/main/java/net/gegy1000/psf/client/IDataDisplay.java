package net.gegy1000.psf.client;

import net.gegy1000.psf.api.data.IModuleDataDisplay;
import net.gegy1000.psf.api.data.IModuleDataDisplayFactory;
import net.gegy1000.psf.server.block.data.packet.PacketRequestDisplayUpdate;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.nbt.NBTTagCompound;

public interface IDataDisplay {
    
    default void requestDisplayUpdate(IModuleDataDisplayFactory factory, IModuleDataDisplay display) {
    	PSFNetworkHandler.network.sendToServer(new PacketRequestDisplayUpdate(factory, display));
    }

	void updateDisplay(NBTTagCompound updateData);
}

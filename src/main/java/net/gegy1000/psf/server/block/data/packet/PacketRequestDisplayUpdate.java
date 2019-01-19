package net.gegy1000.psf.server.block.data.packet;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.data.IModuleDataDisplay;
import net.gegy1000.psf.api.data.IModuleDataDisplayFactory;
import net.gegy1000.psf.server.modules.data.ModuleDisplays;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

@NoArgsConstructor
public class PacketRequestDisplayUpdate implements IMessage {
    
	private IModuleDataDisplayFactory factory;
    private NBTTagCompound requestData;
    
    public PacketRequestDisplayUpdate(IModuleDataDisplayFactory factory, IModuleDataDisplay display) {
    	this.factory = factory;
    	this.requestData = display.getRequestData();
    }
    
    @Override
    public void toBytes(ByteBuf buf) {
    	ByteBufUtils.writeRegistryEntry(buf, factory);
        ByteBufUtils.writeTag(buf, requestData);
    }
    
    @Override
    public void fromBytes(ByteBuf buf) {
    	this.factory = ByteBufUtils.readRegistryEntry(buf, ModuleDisplays.get());
    	this.requestData = ByteBufUtils.readTag(buf);
    }
    
    public static class Handler implements IMessageHandler<PacketRequestDisplayUpdate, IMessage> {
        
        @Override
        public IMessage onMessage(PacketRequestDisplayUpdate message, MessageContext ctx) {
            PracticalSpaceFireworks.PROXY.handlePacket(ctx, 
                    player -> PSFNetworkHandler.network.sendTo(new PacketDisplayUpdate(message.factory.getUpdateData(message.requestData)), (EntityPlayerMP) player));
            return null;
        }
    }
}

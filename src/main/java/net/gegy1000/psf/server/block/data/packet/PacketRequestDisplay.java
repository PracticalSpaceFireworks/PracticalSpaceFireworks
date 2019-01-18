package net.gegy1000.psf.server.block.data.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.api.data.IModuleDataDisplayFactory;
import net.gegy1000.psf.server.modules.data.ModuleDisplays;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

@NoArgsConstructor
@AllArgsConstructor
public class PacketRequestDisplay implements IMessage {
    
    private IModuleDataDisplayFactory factory;
    
    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeRegistryEntry(buf, factory);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.factory = ByteBufUtils.readRegistryEntry(buf, ModuleDisplays.get());
    }
    
    public static class Handler implements IMessageHandler<PacketRequestDisplay, IMessage> {
        
        @Override
        public IMessage onMessage(PacketRequestDisplay message, MessageContext ctx) {
            PracticalSpaceFireworks.PROXY.handlePacket(ctx, 
                    player -> PSFNetworkHandler.network.sendTo(new PacketDisplay(message.factory, message.factory.create(PracticalSpaceFireworks.PROXY.getSatellites()).orElse(message.factory.createDefault())), (EntityPlayerMP) player));
            return null;
        }
    }
}

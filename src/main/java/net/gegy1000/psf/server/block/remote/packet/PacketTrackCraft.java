package net.gegy1000.psf.server.block.remote.packet;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.ISatellite;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

@NoArgsConstructor
@AllArgsConstructor
public class PacketTrackCraft implements IMessage {
    
    private UUID id;
    private boolean track;
    
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(id.getMostSignificantBits());
        buf.writeLong(id.getLeastSignificantBits());
        buf.writeBoolean(track);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.id = new UUID(buf.readLong(), buf.readLong());
        this.track = buf.readBoolean();
    }
    
    public static class Handler implements IMessageHandler<PacketTrackCraft, IMessage> {
        
        @Override
        public IMessage onMessage(PacketTrackCraft message, MessageContext ctx) {
            PracticalSpaceFireworks.PROXY.handlePacket(ctx, player -> {
                EntityPlayerMP p = (EntityPlayerMP) player;
                ISatellite craft = PracticalSpaceFireworks.PROXY.getSatellites().get(message.id);
                if (craft != null) {
                    if (message.track) {
                        craft.track(p);
                    } else {
                        craft.untrack(p);
                    }
                }
            });
            return null;
        }
        
    }
}

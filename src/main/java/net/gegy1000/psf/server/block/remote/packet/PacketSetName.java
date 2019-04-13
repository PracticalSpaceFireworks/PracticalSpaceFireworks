package net.gegy1000.psf.server.block.remote.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.spacecraft.ISatellite;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
public class PacketSetName implements IMessage {
    private UUID uuid;
    private String name;

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.uuid.getMostSignificantBits());
        buf.writeLong(this.uuid.getLeastSignificantBits());
        ByteBufUtils.writeUTF8String(buf, this.name);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.uuid = new UUID(buf.readLong(), buf.readLong());
        this.name = ByteBufUtils.readUTF8String(buf);
    }

    public static class Handler implements IMessageHandler<PacketSetName, IMessage> {
        @Override
        public IMessage onMessage(PacketSetName message, MessageContext ctx) {
            PracticalSpaceFireworks.PROXY.handlePacket(ctx, player -> {
                ISatellite satellite = PracticalSpaceFireworks.PROXY.getSatellites().get(message.uuid);
                if (satellite != null) {
                    satellite.setName(message.name);
                }
            });
            return null;
        }
    }
}

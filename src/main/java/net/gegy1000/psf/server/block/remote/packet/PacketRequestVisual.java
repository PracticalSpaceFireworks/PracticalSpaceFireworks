package net.gegy1000.psf.server.block.remote.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.spacecraft.ISatellite;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class PacketRequestVisual implements IMessage {
    private UUID uuid;

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.uuid.getMostSignificantBits());
        buf.writeLong(this.uuid.getLeastSignificantBits());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.uuid = new UUID(buf.readLong(), buf.readLong());
    }

    public static class Handler implements IMessageHandler<PacketRequestVisual, IMessage> {
        @Override
        public IMessage onMessage(PacketRequestVisual message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                EntityPlayerMP player = ctx.getServerHandler().player;
                ISatellite satellite = PracticalSpaceFireworks.PROXY.getSatellites().get(message.uuid);

                if (satellite != null) {
                    PSFNetworkHandler.network.sendTo(new PacketVisualData(satellite.buildVisual()), player);
                }
            });

            return null;
        }
    }
}

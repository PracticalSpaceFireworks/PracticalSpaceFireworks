package net.gegy1000.psf.server.block.remote.orbiting;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.capability.world.CapabilityWorldData;
import net.gegy1000.psf.server.capability.world.SatelliteWorldData;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
public class PacketSetNameOrbiting implements IMessage {
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

    public static class Handler implements IMessageHandler<PacketSetNameOrbiting, IMessage> {
        @Override
        public IMessage onMessage(PacketSetNameOrbiting message, MessageContext ctx) {
            PracticalSpaceFireworks.PROXY.handlePacket(ctx, player -> {
                World world = player.world;
                SatelliteWorldData worldData = world.getCapability(CapabilityWorldData.SATELLITE_INSTANCE, null);

                if (worldData != null) {
                    ISatellite satellite = worldData.getSatellite(message.uuid);
                    if (satellite != null) {
                        satellite.setName(message.name);
                    }
                }
            });
            return null;
        }
    }
}

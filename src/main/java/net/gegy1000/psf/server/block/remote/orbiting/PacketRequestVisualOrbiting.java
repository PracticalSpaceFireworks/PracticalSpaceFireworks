package net.gegy1000.psf.server.block.remote.orbiting;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.block.remote.PacketVisualData;
import net.gegy1000.psf.server.capability.world.CapabilityWorldData;
import net.gegy1000.psf.server.capability.world.SatelliteWorldData;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class PacketRequestVisualOrbiting implements IMessage {
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

    public static class Handler implements IMessageHandler<PacketRequestVisualOrbiting, IMessage> {
        @Override
        public IMessage onMessage(PacketRequestVisualOrbiting message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                EntityPlayerMP player = ctx.getServerHandler().player;
                World world = player.world;

                SatelliteWorldData worldData = world.getCapability(CapabilityWorldData.SATELLITE_INSTANCE, null);
                if (worldData != null) {
                    ISatellite satellite = worldData.getSatellite(message.uuid);

                    if (satellite != null) {
                        PSFNetworkHandler.network.sendTo(new PacketVisualData(satellite.buildBlockAccess(world), satellite.getModules()), player);
                    }
                }
            });

            return null;
        }
    }
}

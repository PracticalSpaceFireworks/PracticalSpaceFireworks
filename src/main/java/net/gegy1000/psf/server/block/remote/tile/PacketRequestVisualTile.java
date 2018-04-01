package net.gegy1000.psf.server.block.remote.tile;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.block.remote.PacketVisualData;
import net.gegy1000.psf.server.capability.CapabilitySatellite;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

@NoArgsConstructor
@AllArgsConstructor
public class PacketRequestVisualTile implements IMessage {

    private BlockPos satellitePos = BlockPos.ORIGIN;

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(satellitePos.toLong());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.satellitePos = BlockPos.fromLong(buf.readLong());
    }

    public static class Handler implements IMessageHandler<PacketRequestVisualTile, IMessage> {

        @Override
        public IMessage onMessage(PacketRequestVisualTile message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                World world = ctx.getServerHandler().player.world;

                if (world.isBlockLoaded(message.satellitePos)) {
                    TileEntity te = world.getTileEntity(message.satellitePos);
                    if (te != null && te.hasCapability(CapabilitySatellite.INSTANCE, null)) {
                        ISatellite satellite = te.getCapability(CapabilitySatellite.INSTANCE, null);

                        PacketVisualData packet = new PacketVisualData(satellite.buildBlockAccess(world), satellite.getModules());
                        PSFNetworkHandler.network.sendTo(packet, ctx.getServerHandler().player);
                    }
                }
            });

            return null;
        }
    }
}

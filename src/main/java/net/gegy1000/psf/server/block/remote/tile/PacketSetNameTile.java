package net.gegy1000.psf.server.block.remote.tile;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.block.controller.TileController;
import net.gegy1000.psf.server.capability.CapabilitySatellite;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

@AllArgsConstructor
@NoArgsConstructor
public class PacketSetNameTile implements IMessage {

    private BlockPos pos;
    private String name;

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        ByteBufUtils.writeUTF8String(buf, name);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        this.name = ByteBufUtils.readUTF8String(buf);
    }

    public static class Handler implements IMessageHandler<PacketSetNameTile, IMessage> {

        @Override
        public IMessage onMessage(PacketSetNameTile message, MessageContext ctx) {
            PracticalSpaceFireworks.PROXY.handlePacket(ctx, player -> {
                World world = player.world;
                BlockPos pos = message.pos;
                if (world.isBlockLoaded(pos)) {
                    TileEntity te = world.getTileEntity(pos);
                    if (te instanceof TileController) {
                        te.getCapability(CapabilitySatellite.INSTANCE, null).setName(message.name);
                        if (ctx.side.isServer()) {
                            int dimension = world.provider.getDimension();
                            PSFNetworkHandler.network.sendToDimension(message, dimension);
                        }
                    }
                }
            });
            return null;
        }
    }
}

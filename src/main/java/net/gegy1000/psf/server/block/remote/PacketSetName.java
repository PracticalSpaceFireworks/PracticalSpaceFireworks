package net.gegy1000.psf.server.block.remote;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.server.block.controller.TileController;
import net.gegy1000.psf.server.capability.CapabilitySatellite;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

@AllArgsConstructor
@NoArgsConstructor
public class PacketSetName implements IMessage {

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

    public static class Handler implements IMessageHandler<PacketSetName, IMessage> {

        @Override
        public IMessage onMessage(PacketSetName message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                World world = ctx.getServerHandler().player.world;
                if (world.isBlockLoaded(message.pos)) {
                    TileEntity te = world.getTileEntity(message.pos);
                    if (te != null && te instanceof TileController) {
                        te.getCapability(CapabilitySatellite.INSTANCE, null).setName(message.name);
                    }
                }
            });
            return null;
        }

    }
}

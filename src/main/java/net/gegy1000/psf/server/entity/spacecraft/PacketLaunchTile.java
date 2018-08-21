package net.gegy1000.psf.server.entity.spacecraft;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.block.controller.TileController;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

@NoArgsConstructor
@AllArgsConstructor
public class PacketLaunchTile implements IMessage {
    private BlockPos pos;

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
    }

    public static class Handler implements IMessageHandler<PacketLaunchTile, IMessage> {
        @Override
        public IMessage onMessage(PacketLaunchTile message, MessageContext ctx) {
            PracticalSpaceFireworks.PROXY.handlePacket(ctx, player -> {
                if (player.world.isBlockLoaded(message.pos)) {
                    TileEntity tile = player.world.getTileEntity(message.pos);
                    if (tile instanceof TileController) {
                        EntitySpacecraft spacecraft = ((TileController) tile).constructEntity();
                        spacecraft.setState(EntitySpacecraft.StateType.LAUNCH);
                    }
                }
            });
            return null;
        }
    }
}

package net.gegy1000.psf.server.modules.data;

import javax.annotation.Nonnull;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.client.render.laser.LaserRenderer;
import net.gegy1000.psf.client.render.laser.LaserRenderer.LaserState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

@NoArgsConstructor
@AllArgsConstructor
public class PacketLaserState implements IMessage {
    
    private @Nonnull BlockPos loc;
    private @Nonnull LaserState state;
    
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(loc.toLong());
        buf.writeByte(state.ordinal());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.loc = BlockPos.fromLong(buf.readLong());
        this.state = LaserState.values()[buf.readByte()];
    }
    
    public static class Handler implements IMessageHandler<PacketLaserState, IMessage> {
        
        @Override
        public IMessage onMessage(PacketLaserState message, MessageContext ctx) {
            PracticalSpaceFireworks.PROXY.handlePacket(ctx, player -> {
                LaserRenderer.updateLaser(message.loc, message.state);
            });
            return null;
        }
    }
}

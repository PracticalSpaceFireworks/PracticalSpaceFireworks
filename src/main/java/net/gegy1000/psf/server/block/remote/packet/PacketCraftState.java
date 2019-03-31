package net.gegy1000.psf.server.block.remote.packet;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.client.IVisualReceiver;
import net.gegy1000.psf.api.spacecraft.IListedSpacecraft;
import net.gegy1000.psf.server.block.remote.orbiting.OrbitingListedSpacecraft;
import net.gegy1000.psf.server.block.remote.packet.PacketOpenRemoteControl.SatelliteState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

@NoArgsConstructor
@AllArgsConstructor
public class PacketCraftState implements IMessage {
    
    private SatelliteState type;
    private IListedSpacecraft craft;

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(type.ordinal());
        buf.writeLong(craft.getId().getMostSignificantBits());
        buf.writeLong(craft.getId().getLeastSignificantBits());
        buf.writeLong(craft.getPosition().toLong());
        ByteBufUtils.writeUTF8String(buf, craft.getName());
    }
    
    @Override
    public void fromBytes(ByteBuf buf) {
        this.type = SatelliteState.values()[buf.readByte()];
        UUID uuid = new UUID(buf.readLong(), buf.readLong());
        BlockPos pos = BlockPos.fromLong(buf.readLong());
        String name = ByteBufUtils.readUTF8String(buf);
        this.craft = new OrbitingListedSpacecraft(name, pos, uuid);
    }
    
    public static class Handler implements IMessageHandler<PacketCraftState, IMessage> {
        
        @Override
        public IMessage onMessage(PacketCraftState message, MessageContext ctx) {
            PracticalSpaceFireworks.PROXY.handlePacket(ctx, player -> {
                GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
                if (currentScreen instanceof IVisualReceiver) {
                    IVisualReceiver receiver = (IVisualReceiver) currentScreen;
                    if (message.type == SatelliteState.DESTROYED) {
                        receiver.removeCraft(message.craft.getId());
                    } else {
                        receiver.updateCraft(SatelliteState.getCraft(player, message.type, message.craft));
                    }
                }
            });
            return null;
        }
    }
}

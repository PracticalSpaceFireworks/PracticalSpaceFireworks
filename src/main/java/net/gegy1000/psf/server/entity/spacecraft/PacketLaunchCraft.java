package net.gegy1000.psf.server.entity.spacecraft;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

@NoArgsConstructor
@AllArgsConstructor
public class PacketLaunchCraft implements IMessage {
    private int entityId;

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
    }

    public static class Handler implements IMessageHandler<PacketLaunchCraft, IMessage> {
        @Override
        public IMessage onMessage(PacketLaunchCraft message, MessageContext ctx) {
            PracticalSpaceFireworks.PROXY.handlePacket(ctx, player -> {
                Entity entity = player.world.getEntityByID(message.entityId);
                if (entity instanceof EntitySpacecraft) {
                    ((EntitySpacecraft) entity).setState(EntitySpacecraft.StateType.LAUNCH);
                }
            });
            return null;
        }
    }
}

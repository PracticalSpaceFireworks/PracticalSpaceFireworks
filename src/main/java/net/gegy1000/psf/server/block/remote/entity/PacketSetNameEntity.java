package net.gegy1000.psf.server.block.remote.entity;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
public class PacketSetNameEntity implements IMessage {
    private int entityId;
    private String name;

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
        ByteBufUtils.writeUTF8String(buf, this.name);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
        this.name = ByteBufUtils.readUTF8String(buf);
    }

    public static class Handler implements IMessageHandler<PacketSetNameEntity, IMessage> {
        @Override
        public IMessage onMessage(PacketSetNameEntity message, MessageContext ctx) {
            PracticalSpaceFireworks.PROXY.handlePacket(ctx, player -> {
                World world = player.world;
                int entityId = message.entityId;
                Entity entity = world.getEntityByID(entityId);
                if (entity instanceof EntitySpacecraft) {
                    EntitySpacecraft spacecraft = (EntitySpacecraft) entity;
                    spacecraft.getSatellite().setName(message.name);
                    if (ctx.side.isServer()) {
                        WorldServer serverWorld = ctx.getServerHandler().player.getServerWorld();
                        Set<? extends EntityPlayer> trackingPlayers = serverWorld.getEntityTracker().getTrackingPlayers(spacecraft);
                        for (EntityPlayer trackingPlayer : trackingPlayers) {
                            if (trackingPlayer instanceof EntityPlayerMP) {
                                PSFNetworkHandler.network.sendTo(message, (EntityPlayerMP) trackingPlayer);
                            }
                        }
                    }
                }
            });
            return null;
        }
    }
}

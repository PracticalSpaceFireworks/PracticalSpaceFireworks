package net.gegy1000.psf.server.block.remote;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.block.remote.orbiting.OrbitingListedSpacecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
public class PacketOpenRemoteControl implements IMessage {
    private BlockPos pos;
    private Collection<ISatellite> crafts;

    private final List<IListedSpacecraft> listedCrafts = new ArrayList<>();

    public PacketOpenRemoteControl(BlockPos pos, Collection<ISatellite> crafts) {
        this.pos = pos;
        this.crafts = crafts;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.pos.toLong());
        buf.writeShort(this.crafts.size() & 0xFFFF);
        for (ISatellite satellite : this.crafts) {
            buf.writeLong(satellite.getId().getMostSignificantBits());
            buf.writeLong(satellite.getId().getLeastSignificantBits());
            buf.writeLong(satellite.getPosition().toLong());
            ByteBufUtils.writeUTF8String(buf, satellite.getName());
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = BlockPos.fromLong(buf.readLong());
        int count = buf.readUnsignedShort();
        for (int i = 0; i < count; i++) {
            UUID uuid = new UUID(buf.readLong(), buf.readLong());
            BlockPos pos = BlockPos.fromLong(buf.readLong());
            String name = ByteBufUtils.readUTF8String(buf);
            this.listedCrafts.add(new OrbitingListedSpacecraft(name, pos, uuid));
        }
    }

    public static class Handler implements IMessageHandler<PacketOpenRemoteControl, IMessage> {
        @Override
        public IMessage onMessage(PacketOpenRemoteControl message, MessageContext ctx) {
            this.updateCraftListClient(message);
            return null;
        }

        @SideOnly(Side.CLIENT)
        private void updateCraftListClient(PacketOpenRemoteControl message) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                EntityPlayer player = Minecraft.getMinecraft().player;

                BlockPos pos = message.pos;
                if (player.world.isBlockLoaded(pos) && player.getDistanceSqToCenter(pos) <= 64.0) {
                    TileEntity entity = player.world.getTileEntity(pos);
                    if (entity instanceof TileRemoteControlSystem) {
                        TileRemoteControlSystem controlSystem = (TileRemoteControlSystem) entity;
                        controlSystem.provideServerListedCrafts(message.listedCrafts);
                        Minecraft.getMinecraft().displayGuiScreen(new GuiControlSystem(new ContainerControlSystem(controlSystem, player.inventory)));
                    }
                }
            });
        }
    }
}

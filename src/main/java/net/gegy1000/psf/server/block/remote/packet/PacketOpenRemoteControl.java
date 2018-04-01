package net.gegy1000.psf.server.block.remote.packet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.gegy1000.psf.api.ISatellite;
import net.gegy1000.psf.server.block.remote.ContainerControlSystem;
import net.gegy1000.psf.server.block.remote.GuiControlSystem;
import net.gegy1000.psf.server.block.remote.IListedSpacecraft;
import net.gegy1000.psf.server.block.remote.TileRemoteControlSystem;
import net.gegy1000.psf.server.block.remote.entity.EntityListedSpacecraft;
import net.gegy1000.psf.server.block.remote.orbiting.OrbitingListedSpacecraft;
import net.gegy1000.psf.server.block.remote.tile.TileListedSpacecraft;
import net.gegy1000.psf.server.capability.CapabilitySatellite;
import net.gegy1000.psf.server.entity.spacecraft.EntitySpacecraft;
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

@NoArgsConstructor
public class PacketOpenRemoteControl implements IMessage {
    
    @RequiredArgsConstructor
    public enum SatelliteState {
        TILE(TileListedSpacecraft.class),
        ENTITY(EntityListedSpacecraft.class),
        ORBIT(OrbitingListedSpacecraft.class),
        ;
        
        private final Class<? extends IListedSpacecraft> clazz;
        
        public static SatelliteState byClass(Class<? extends IListedSpacecraft> cls) {
            for (SatelliteState state : values()) {
                if (state.clazz == cls) {
                    return state;
                }
            }
            return ORBIT;
        }
    }
    
    private BlockPos pos;
    private Collection<ISatellite> crafts;

    @Nonnull
    private Map<SatelliteState, List<IListedSpacecraft>> byType = new HashMap<>();

    public PacketOpenRemoteControl(BlockPos pos, @Nonnull EnumMap<SatelliteState, List<IListedSpacecraft>> crafts) {
        this.byType = crafts;
        this.pos = pos;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.pos.toLong());
        buf.writeShort(this.byType.size() & 0xFFFF);
        for (val e : this.byType.entrySet()) {
            for (IListedSpacecraft satellite : e.getValue()) {
                buf.writeLong(satellite.getId().getMostSignificantBits());
                buf.writeLong(satellite.getId().getLeastSignificantBits());
                buf.writeLong(satellite.getPosition().toLong());
                ByteBufUtils.writeUTF8String(buf, satellite.getName());
                buf.writeByte(e.getKey().ordinal());
            }
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
            SatelliteState type = SatelliteState.values()[buf.readByte()];
            this.byType.computeIfAbsent(type, t -> new ArrayList<>()).add(new OrbitingListedSpacecraft(name, pos, uuid));
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
                        // FIXME
                        List<IListedSpacecraft> allCrafts = new ArrayList<>();
                        for (val e : message.byType.entrySet()) {
                            for (IListedSpacecraft craft : e.getValue()) {
                                switch(e.getKey()) {
                                case TILE:
                                    TileEntity te = player.world.getTileEntity(craft.getPosition());
                                    if (te != null && te.hasCapability(CapabilitySatellite.INSTANCE, null)) {
                                        allCrafts.add(new TileListedSpacecraft(te.getCapability(CapabilitySatellite.INSTANCE, null)));
                                    }
                                    break;
                                case ENTITY:
                                    List<EntitySpacecraft> entities = player.world.getEntities(EntitySpacecraft.class, ent -> ent.getUniqueID().equals(craft.getId()));
                                    if (!entities.isEmpty()) {
                                        allCrafts.add(new EntityListedSpacecraft(entities.get(0)));
                                    }
                                    break;
                                default:
                                    allCrafts.add(craft);
                                }
                            }
                        }
                        controlSystem.provideServerCrafts(allCrafts);
                        Minecraft.getMinecraft().displayGuiScreen(new GuiControlSystem(new ContainerControlSystem(controlSystem, player.inventory)));
                    }
                }
            });
        }
    }
}

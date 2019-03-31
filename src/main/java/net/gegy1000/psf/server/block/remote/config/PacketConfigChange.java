package net.gegy1000.psf.server.block.remote.config;

import java.util.Optional;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.IListedSpacecraft;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.IModuleConfig;
import net.gegy1000.psf.api.ISatellite;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

@NoArgsConstructor
public class PacketConfigChange implements IMessage {
    
    private UUID craftId;
    private UUID moduleId;
    private String cfgKey;
    private NBTTagCompound cfgData;
    
    public PacketConfigChange(IListedSpacecraft iListedSpacecraft, IModule module, IModuleConfig cfg) {
        this.craftId = iListedSpacecraft.getId();
        this.moduleId = module.getId();
        this.cfgKey = cfg.getKey();
        this.cfgData = cfg.serializeNBT();
    }
    
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(craftId.getMostSignificantBits());
        buf.writeLong(craftId.getLeastSignificantBits());
        buf.writeLong(moduleId.getMostSignificantBits());
        buf.writeLong(moduleId.getLeastSignificantBits());
        ByteBufUtils.writeUTF8String(buf, cfgKey);
        ByteBufUtils.writeTag(buf, cfgData);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.craftId = new UUID(buf.readLong(), buf.readLong());
        this.moduleId = new UUID(buf.readLong(), buf.readLong());
        this.cfgKey = ByteBufUtils.readUTF8String(buf);
        this.cfgData = ByteBufUtils.readTag(buf);
    }
    
    public static class Handler implements IMessageHandler<PacketConfigChange, IMessage> {
        
        @Override
        public IMessage onMessage(PacketConfigChange message, MessageContext ctx) {
            PracticalSpaceFireworks.PROXY.handlePacket(ctx, player -> {
                Optional.of(PracticalSpaceFireworks.PROXY.getSatellites().get(message.craftId))
                        .map(ISatellite::getIndexedModules)
                        .map(map -> map.get(message.moduleId))
                        .map(m -> m.getConfig(message.cfgKey))
                        .ifPresent(cfg -> {
                            cfg.deserializeNBT(message.cfgData);
                            cfg.modified();
                        });
            });
            return null;
        }
    }
}

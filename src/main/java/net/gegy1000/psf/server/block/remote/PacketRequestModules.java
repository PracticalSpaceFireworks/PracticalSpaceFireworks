package net.gegy1000.psf.server.block.remote;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.server.block.controller.TileController;
import net.gegy1000.psf.server.capability.CapabilitySatellite;
import net.gegy1000.psf.server.network.PSFNetworkHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

@NoArgsConstructor
@AllArgsConstructor
public class PacketRequestModules implements IMessage {
    
    private BlockPos satellitePos = BlockPos.ORIGIN;
    
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(satellitePos.toLong());
    }
    
    @Override
    public void fromBytes(ByteBuf buf) {
        this.satellitePos = BlockPos.fromLong(buf.readLong());
    }

    public static class Handler implements IMessageHandler<PacketRequestModules, IMessage> {
        
        @Override
        public IMessage onMessage(PacketRequestModules message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
               TileEntity te = ctx.getServerHandler().player.world.getTileEntity(message.satellitePos);
               if (te != null && te.hasCapability(CapabilitySatellite.INSTANCE, null)) {
                   PSFNetworkHandler.network.sendTo(new PacketModules(te.getCapability(CapabilitySatellite.INSTANCE, null).getModules()), ctx.getServerHandler().player);
               }
            });
            return null;
        }
    }
}

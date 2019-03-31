package net.gegy1000.psf.server.satellite;

import javax.annotation.Nonnull;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.client.IVisualReceiver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NoArgsConstructor
@AllArgsConstructor
public class PacketModule implements IMessage {
    
    @Getter
    private @Nonnull UUID id;
    @Getter(AccessLevel.PROTECTED)
    private @Nonnull NBTTagCompound tag;
    
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(id.getMostSignificantBits());
        buf.writeLong(id.getLeastSignificantBits());
        ByteBufUtils.writeTag(buf, tag);
    }
    
    
    @Override
    public void fromBytes(ByteBuf buf) {
        this.id = new UUID(buf.readLong(), buf.readLong());
        this.tag = ByteBufUtils.readTag(buf);
    }

    @RequiredArgsConstructor
    public static class Handler implements IMessageHandler<PacketModule, IMessage> {
        
        @Override
        public IMessage onMessage(PacketModule message, MessageContext ctx) {
            PracticalSpaceFireworks.PROXY.handlePacket(ctx, player -> updateModuleClient(message));
            return null;
        }
        
        @SideOnly(Side.CLIENT)
        private void updateModuleClient(PacketModule message) {
            GuiScreen gui = Minecraft.getMinecraft().currentScreen;
            if (gui instanceof IVisualReceiver) {
                ((IVisualReceiver) gui).updateModule(message.getId(), message.getTag());
            }
        }
    }
}

package net.gegy1000.psf.server.block.remote.packet;

import java.util.List;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.api.IListedSpacecraft;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.ISpacecraftBodyData;
import net.gegy1000.psf.api.IVisualReceiver;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftBodyData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AllArgsConstructor
@NoArgsConstructor
public class PacketVisualData implements IMessage {
    private ISpacecraftBodyData bodyData;

    @Override
    public void toBytes(ByteBuf buf) {
        bodyData.serialize(buf);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        bodyData = SpacecraftBodyData.deserializeCraft(buf);
    }
    
    public static class Handler implements IMessageHandler<PacketVisualData, IMessage> {
        
        @Override
        public IMessage onMessage(PacketVisualData message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> updateVisualClient(message));
            return null;
        }
        
        @SideOnly(Side.CLIENT)
        private void updateVisualClient(PacketVisualData message) {
            GuiScreen gui = Minecraft.getMinecraft().currentScreen;
            if (gui instanceof IVisualReceiver) {
                List<IModule> modules = message.bodyData.findModules();
                ((IVisualReceiver) gui).setVisual(new IListedSpacecraft.Visual(message.bodyData, modules));
            }
        }
    }
}

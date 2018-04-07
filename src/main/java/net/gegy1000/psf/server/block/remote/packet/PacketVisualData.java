package net.gegy1000.psf.server.block.remote.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.client.IVisualReceiver;
import net.gegy1000.psf.server.block.remote.IListedSpacecraft;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftWorldHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class PacketVisualData implements IMessage {
    private SpacecraftWorldHandler worldHandler;

    @Override
    public void toBytes(ByteBuf buf) {
        worldHandler.serialize(buf);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        worldHandler = SpacecraftWorldHandler.deserializeCraft(buf);
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
                List<IModule> modules = message.worldHandler.findModules();
                ((IVisualReceiver) gui).setVisual(new IListedSpacecraft.Visual(message.worldHandler, modules));
            }
        }
    }
}

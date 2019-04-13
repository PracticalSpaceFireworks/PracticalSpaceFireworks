package net.gegy1000.psf.server.block.remote.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.api.client.IVisualData;
import net.gegy1000.psf.api.client.IVisualReceiver;
import net.gegy1000.psf.server.block.remote.visual.VisualData;
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
    private IVisualData visual;

    @Override
    public void toBytes(ByteBuf buf) {
        visual.serialize(buf);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        visual = VisualData.deserialize(buf);
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
                ((IVisualReceiver) gui).setVisual(message.visual);
            }
        }
    }
}

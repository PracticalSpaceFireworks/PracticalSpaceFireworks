package net.gegy1000.psf.server.network;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.client.gui.GuiContainerMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

@NoArgsConstructor
@AllArgsConstructor
public class PacketDisplayContainerMessage implements IMessage {
    private ITextComponent title;
    private ITextComponent[] lines;

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuf = new PacketBuffer(buf);
        packetBuf.writeTextComponent(this.title);

        packetBuf.writeByte(this.lines.length & 0xFF);
        for (ITextComponent component : this.lines) {
            packetBuf.writeTextComponent(component);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuf = new PacketBuffer(buf);
        try {
            this.title = packetBuf.readTextComponent();

            this.lines = new ITextComponent[packetBuf.readUnsignedByte()];
            for (int i = 0; i < this.lines.length; i++) {
                this.lines[i] = packetBuf.readTextComponent();
            }
        } catch (IOException e) {
            PracticalSpaceFireworks.LOGGER.error("Failed to parse text components", e);
        }
    }

    public static class Handler implements IMessageHandler<PacketDisplayContainerMessage, IMessage> {
        @Override
        public IMessage onMessage(PacketDisplayContainerMessage message, MessageContext ctx) {
            this.displayGuiClient(message);
            return null;
        }

        @SideOnly(Side.CLIENT)
        private void displayGuiClient(PacketDisplayContainerMessage message) {
            Minecraft client = Minecraft.getMinecraft();
            client.addScheduledTask(() -> client.displayGuiScreen(new GuiContainerMessage(message.title, message.lines)));
        }
    }
}

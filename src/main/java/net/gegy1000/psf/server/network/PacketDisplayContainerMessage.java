package net.gegy1000.psf.server.network;

import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Singular;
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

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PacketDisplayContainerMessage implements IMessage {
    private ITextComponent title;
    @Singular
    private ImmutableList<ITextComponent> lines;

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuf = new PacketBuffer(buf);
        packetBuf.writeTextComponent(this.title);
        packetBuf.writeByte(this.lines.size() & 0xFF);
        this.lines.forEach(packetBuf::writeTextComponent);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        final PacketBuffer packetBuf = new PacketBuffer(buf);
        try {
            this.title = packetBuf.readTextComponent();
            ImmutableList.Builder<ITextComponent> lines = ImmutableList.builder();
            short size = packetBuf.readUnsignedByte();
            for (int i = 0; i < size; i++) {
                lines.add(packetBuf.readTextComponent());
            }
            this.lines = lines.build();
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
        private void displayGuiClient(PacketDisplayContainerMessage msg) {
            Minecraft client = Minecraft.getMinecraft();
            client.addScheduledTask(() -> client.displayGuiScreen(new GuiContainerMessage(msg.title, msg.lines)));
        }
    }
}

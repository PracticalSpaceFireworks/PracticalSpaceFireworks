package net.gegy1000.psf.server.block.data.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.api.data.IModuleDataDisplay;
import net.gegy1000.psf.api.data.IModuleDataDisplayFactory;
import net.gegy1000.psf.client.IDataDisplay;
import net.gegy1000.psf.server.modules.data.ModuleDisplays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;


@AllArgsConstructor
@NoArgsConstructor
public class PacketDisplay implements IMessage {
    
    private IModuleDataDisplayFactory factory;
    private IModuleDataDisplay display;

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeRegistryEntry(buf, factory);
        ByteBufUtils.writeTag(buf, display.serializeNBT());
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        factory = ByteBufUtils.readRegistryEntry(buf, ModuleDisplays.get());
        display = factory.createDefault();
        display.deserializeNBT(ByteBufUtils.readTag(buf));
    }
    
    public static class Handler implements IMessageHandler<PacketDisplay, IMessage> {

        @Override
        public IMessage onMessage(PacketDisplay message, MessageContext ctx) {
            PracticalSpaceFireworks.PROXY.handlePacket(ctx, player -> {
                GuiScreen gui = Minecraft.getMinecraft().currentScreen;
                if (gui instanceof IDataDisplay) {
                    ((IDataDisplay)gui).setDisplay(message.display);
                }
            });
            return null;
        }
        
    }
}

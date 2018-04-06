package net.gegy1000.psf.server.block.remote.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.api.IModule;
import net.gegy1000.psf.api.IModuleFactory;
import net.gegy1000.psf.client.IVisualReceiver;
import net.gegy1000.psf.server.block.remote.IListedSpacecraft;
import net.gegy1000.psf.server.entity.spacecraft.SpacecraftWorldHandler;
import net.gegy1000.psf.server.modules.Modules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collection;

@AllArgsConstructor
@NoArgsConstructor
public class PacketVisualData implements IMessage {
    private SpacecraftWorldHandler worldHandler;
    private Collection<IModule> modules = new ArrayList<>();

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(modules.size());
        for (IModule m : modules) {
            ByteBufUtils.writeUTF8String(buf, m.getRegistryName().toString());
            ByteBufUtils.writeTag(buf, m.serializeNBT());
        }
        worldHandler.serialize(buf);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            ResourceLocation id = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
            NBTTagCompound tag = ByteBufUtils.readTag(buf);
            IModuleFactory factory = Modules.get().getValue(id);
            if (factory != null) {
                IModule m = factory.get();
                m.deserializeNBT(tag);
                modules.add(m);
            }
        }
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
                ((IVisualReceiver) gui).setVisual(new IListedSpacecraft.Visual(message.worldHandler, message.modules));
            }
        }
    }
}

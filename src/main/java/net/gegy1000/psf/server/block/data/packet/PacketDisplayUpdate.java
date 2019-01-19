package net.gegy1000.psf.server.block.data.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import net.gegy1000.psf.PracticalSpaceFireworks;
import net.gegy1000.psf.client.IDataDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

@NoArgsConstructor
@AllArgsConstructor
public class PacketDisplayUpdate implements IMessage {
	
	private NBTTagCompound updateData;

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, updateData);
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		this.updateData = ByteBufUtils.readTag(buf);
	}
	
	public static class Handler implements IMessageHandler<PacketDisplayUpdate, IMessage> {
		
		@Override
		public IMessage onMessage(PacketDisplayUpdate message, MessageContext ctx) {
			PracticalSpaceFireworks.PROXY.handlePacket(ctx, $ -> {
				GuiScreen gui = Minecraft.getMinecraft().currentScreen;
				if (gui instanceof IDataDisplay) {
					((IDataDisplay) gui).updateDisplay(message.updateData);
				}
			});
			return null;
		}
	}
}

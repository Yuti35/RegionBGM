package com.yuti.regionbgm.network;

import com.yuti.regionbgm.client.music.BackgroundMusicManager;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This packet is send from the server to the client to ask it to play a musical resource.
 * @author Yuti
 *
 */
public class PacketPlayResource implements IMessage {
	
	private boolean messageValid;
	
	/**
	 * The resource you want to be played (an URL, youtube, twitch, almost anything) 
	 */
	private String resource;
	
	/**
	 * Tells is the resource should loop or not
	 */
	private boolean looping;
	
	public PacketPlayResource() {
		this.messageValid = false;
	}

	public PacketPlayResource(String resource, boolean looping) {
		if(resource != null) {
			this.resource = resource;
			this.looping = looping;
			this.messageValid = true;			
		}
		else {
			this.messageValid = false;
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		this.messageValid = true;
		this.resource = ByteBufUtils.readUTF8String(buf);
		this.looping = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		if (!this.messageValid)
			return;
		ByteBufUtils.writeUTF8String(buf, this.resource);
		buf.writeBoolean(this.looping);
	}
	
	public static class Handler implements IMessageHandler<PacketPlayResource, IMessage> {
		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(PacketPlayResource message, MessageContext ctx) {
			if (!message.messageValid)
				return null;
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler)
					.addScheduledTask(() -> processMessage(message, ctx));
			return null;
		}

		@SideOnly(Side.CLIENT)
		void processMessage(PacketPlayResource message, MessageContext ctx) {
			BackgroundMusicManager.instance.playResource(message.resource, message.looping);
		}
	}

}

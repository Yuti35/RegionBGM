package com.yuti.regionbgm.network;

import com.yuti.regionbgm.client.music.BackgroundMusicManager;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This packet is send from the server to the client to ask it to stop playing the current resource (will stop the BGM on client-side).
 * @author Yuti
 *
 */
public class PacketStopMusic implements IMessage {

	public PacketStopMusic() {

	}

	@Override
	public void fromBytes(ByteBuf buf) {

	}

	@Override
	public void toBytes(ByteBuf buf) {

	}
	
	public static class Handler implements IMessageHandler<PacketStopMusic, IMessage> {
		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(PacketStopMusic message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler)
					.addScheduledTask(() -> processMessage(message, ctx));
			return null;
		}

		@SideOnly(Side.CLIENT)
		void processMessage(PacketStopMusic message, MessageContext ctx) {
			BackgroundMusicManager.instance.stopMusic();
		}
	}

}

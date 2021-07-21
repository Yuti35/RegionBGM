package com.yuti.regionbgm.network;

import com.yuti.regionbgm.server.events.PlayerAskRefreshBgmEvent;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * A packet send from the client to the server to ask a refresh of the BGM.
 * It will triggers a {@link PlayerAskRefreshBgmEvent}
 * It will mainly be used when a client reactivate the mod after disabling it, to keep it updated with the current region's BGM.
 * @author Yuti
 *
 */
public class PacketAskRefresh implements IMessage {
	
	public PacketAskRefresh() {
		
	}

	@Override
	public void fromBytes(ByteBuf buf) {

	}

	@Override
	public void toBytes(ByteBuf buf) {

	}
	
	public static class Handler implements IMessageHandler<PacketAskRefresh, IMessage> {

		@Override
		public IMessage onMessage(PacketAskRefresh message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler)
					.addScheduledTask(() -> processMessage(message, ctx));
			return null;
		}

		void processMessage(PacketAskRefresh message, MessageContext ctx) {
			EntityPlayer player = ctx.getServerHandler().player;;
			if(player != null) {
				PlayerAskRefreshBgmEvent event = new PlayerAskRefreshBgmEvent(player);
				MinecraftForge.EVENT_BUS.post(event);
			}
		}
	}

}

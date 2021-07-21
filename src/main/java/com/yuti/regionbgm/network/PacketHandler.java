package com.yuti.regionbgm.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {

	public static SimpleNetworkWrapper INSTANCE;

	private static int ID = 0;

	private static int nextID() {
		return ID++;
	}
	
	public static void registerMessages(String channelName) {
		INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(channelName);
		INSTANCE.registerMessage(PacketPlayResource.Handler.class, PacketPlayResource.class, nextID(), Side.CLIENT);
		INSTANCE.registerMessage(PacketStopMusic.Handler.class, PacketStopMusic.class, nextID(), Side.CLIENT);
		INSTANCE.registerMessage(PacketAskRefresh.Handler.class, PacketAskRefresh.class, nextID(), Side.SERVER);
	}
}

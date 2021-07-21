package com.yuti.regionbgm.server.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * An event triggered on server-side after receiving a {@link com.yuti.regionbgm.network.PacketAskRefresh} from the client.
 * It will mainly be used when a client reactivate the mod after disabling it, to keep it updated with the current region's BGM.
 * @author Yuti
 *
 */
public class PlayerAskRefreshBgmEvent extends PlayerEvent {
	public PlayerAskRefreshBgmEvent(EntityPlayer player) {
		super(player);
	}
}

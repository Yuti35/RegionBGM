package com.yuti.regionbgm.server.api;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.yuti.regionbgm.network.PacketHandler;
import com.yuti.regionbgm.network.PacketPlayResource;
import com.yuti.regionbgm.network.PacketStopMusic;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.ValueType;
import net.minecraft.world.World;

/**
 * This class is used by the other server-side mods to triggers actions on clients having RegionBGM installed
 * Basically, it will allows to play musical resources (youtube, twitch, anything...) and stop the music.
 * Each server-side mods need to register and get their own instance of the API through {@link ApiRegionBGM#getInstance(String)}
 * A gamerule is automatically registered (bgm_modid) to allows disable or reactivate the usage of the API for the server-side mod.
 * This class must be only used on server-side
 * @author Yuti
 *
 */
public class ApiRegionBGM {
	
	/**
	 * The gamerule associated the to server-side mod using the API (bgm_modid)
	 */
	private final String gamerule;
	
	/**
	 * Store all the instances of the API for all server-side mods using it.
	 */
	private final static Map<String, ApiRegionBGM> API_MAP = new HashMap<String, ApiRegionBGM>();
	
	/**
	 * 
	 * @param modId The id of the mod using the API.
	 * @return The instance of {@link ApiRegionBGM} associated to the modid
	 */
	@Nullable
	public synchronized static ApiRegionBGM getInstance(String modId) {
		if(modId != null) {
			if(API_MAP.containsKey(modId)) {
				return API_MAP.get(modId);
			}
			else {
				return new ApiRegionBGM(modId);
			}
		}
		
		return null;
	}
	
	private ApiRegionBGM(String modId) {
		this.gamerule = "bgm_" + modId;
	}
	
	/**
	 * Need to be called once during the server's initialization to register the gamerule on the server.
	 * @param server The server which is initializing
	 */
	public void registerOnServer(MinecraftServer server) {
		if(server != null) {
			World world = server.getEntityWorld();
			if(world != null) {
				GameRules rules = world.getGameRules();
				if(rules != null) {
					rules.addGameRule(this.gamerule, "true", ValueType.BOOLEAN_VALUE);					
				}
			}			
		}
	}
	
	/**
	 * Play a musical resource to the player (triggered on server-side).
	 * @param world The world where the player is
	 * @param player The targeted player
	 * @param resource The resource you want to be played (an URL, youtube, twitch, almost anything)
	 * @param looping Tells is the resource should loop or not
	 */
	public void playResource(World world, EntityPlayerMP player, String resource, boolean looping) {
		if(canSendPacket(world)) {
			PacketHandler.INSTANCE.sendTo(new PacketPlayResource(resource, looping), player);				
		}
	}
	
	/**
	 * Stops the music currently playing for the targeted player (triggered on server-side)
	 * @param world The world where the player is
	 * @param player The targeted player
	 */
	public void stopMusic(World world, EntityPlayerMP player) {
		if(canSendPacket(world)) {
			PacketHandler.INSTANCE.sendTo(new PacketStopMusic(), player);		
		}
	}
	
	/**
	 * Tells if the API can send packets or not (based on the gamerule)
	 * @param world The world where the APi is called
	 * @return true if the APi can send packets or false if not.
	 */
	private boolean canSendPacket(World world) {
		if(this.gamerule != null && world != null) {
			GameRules rules = world.getGameRules();
			if(rules != null) {
				if(!rules.hasRule(this.gamerule) || rules.getBoolean(this.gamerule)) {
					return true;
				}
			}
		}
		return false;
	}
}

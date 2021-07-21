package com.yuti.regionbgm.client.music;

import static com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats.COMMON_PCM_S16_BE;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.sound.sampled.AudioInputStream;

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.yuti.regionbgm.client.config.RegionBGMConfig;
import com.yuti.regionbgm.network.PacketAskRefresh;
import com.yuti.regionbgm.network.PacketHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import paulscode.sound.SoundSystem;

/**
 * This manager allows the manage the music on client-side. (Load resource, stop music, etc...)
 * @author Yuti
 *
 */
@SideOnly(Side.CLIENT)
@SuppressWarnings("deprecation")
public class BackgroundMusicManager {

	/**
	 * Instance of the manager.
	 */
	public static final BackgroundMusicManager instance = new BackgroundMusicManager();
	
	/**
	 * The player where the musics are loaded.
	 */
	public final AudioPlayer player;
	
	/**
	 * Manages the playlist of the BGM through the {@link BackgroundMusicManager#player}.
	 */
	public final BackgroundMusicTrackManager trackManager;
	
	private final AudioPlayerManager manager;
	
	/**
	 * The thread used to play the music.
	 */
	private BackgroundMusicThread musicThread;
	
	private static final Object loaderLock = new Object();
	
	/**
	 * Minecraft sound handler (obtained through reflection)
	 */
	private SoundHandler soundHandler;
	
	/**
	 * Minecraft sound system (obtained through reflection)
	 */
	private SoundSystem soundSystem;
	
	/**
	 * A map listing all the sounds currently playing on the client (obtained through reflection)
	 */
	private Map<String, ISound> minecraftPlayingSounds;
	
	@SuppressWarnings({"unchecked"})
	private BackgroundMusicManager() {
		this.manager = new DefaultAudioPlayerManager(); 
		AudioSourceManagers.registerRemoteSources(manager); 
		this.manager.getConfiguration().setOutputFormat(COMMON_PCM_S16_BE);
		this.player = this.manager.createPlayer();
		this.trackManager = new BackgroundMusicTrackManager(this.player);
		this.player.addListener(this.trackManager);
		AudioDataFormat format = this.manager.getConfiguration().getOutputFormat();
		AudioInputStream stream = AudioPlayerInputStream.createStream(this.player, format, 10000L, false);
		this.soundHandler = Minecraft.getMinecraft().getSoundHandler();
		SoundManager soundManager  = (SoundManager) ReflectionHelper.getPrivateValue(SoundHandler.class, this.soundHandler , "sndManager", "field_147694_f");
		this.soundSystem = (SoundSystem) ReflectionHelper.getPrivateValue(SoundManager.class, soundManager, "sndSystem", "field_148620_e");
		this.minecraftPlayingSounds = (Map<String, ISound>)  ReflectionHelper.getPrivateValue(SoundManager.class, soundManager, "playingSounds", "field_148629_h");
		this.musicThread = new BackgroundMusicThread(stream);
		this.musicThread.start();
		this.updateConfiguredMusicVolume();
	}
	
	/**
	 * Play a musical resource on the client (triggered on client-side).
	 * @param resource The resource you want to be played (an URL, youtube, twitch, almost anything)
	 * @param looping Tells is the resource should loop or not
	 */
	public void playResource(String resource, boolean looping) {
		synchronized (loaderLock) {
			if(RegionBGMConfig.enabled && trackManager.updateResource(resource, looping)) {
				this.manager.loadItem(resource, new AudioLoadResultHandler() {			
					
					public boolean afterLoaded() {
						if(RegionBGMConfig.enabled && trackManager.getCurrentLoadedResource().equals(resource)) {
							stopMinecraftMusic();
							return true;
						}
						
						return false;
					}
					
					@Override
					public void trackLoaded(AudioTrack track) {
						synchronized (loaderLock) {
							if(afterLoaded()) {
								trackManager.loadAndPlay(track);
							}						
						}
					}
					
					@Override
					public void playlistLoaded(AudioPlaylist playlist) {
						synchronized (loaderLock) {
							if(afterLoaded()) {
								trackManager.loadAndPlay(playlist);
							}						
						}
					}
					
					@Override
					public void noMatches() {
	
					}
					
					@Override
					public void loadFailed(FriendlyException exception) {
						exception.printStackTrace();
					}
				});
			}
		}
	}
	
	/**
	 * Stops the music currently playing (triggered on client-side)
	 */
	public void stopMusic() {
		synchronized (loaderLock) {
			this.trackManager.stop();			
		}
	}
	
	/**
	 * Set the volume of the BGM
	 * @param volume Volume to set (between 0 and 100)
	 */
	public void setVolume(int volume) {
		updateMusicVolume(volume);
	}
	
	public void updateConfiguredMusicVolume() {
		setVolume(RegionBGMConfig.volume);
	}
	
	/**
	 * Update the current music volume according to the volume specified in the config and global Minecraft's master volume.
	 * @param volume
	 */
	private void updateMusicVolume(int volume) {
		int currentMasterVolume = this.getMinecraftMasterVolume();
		if(volume > currentMasterVolume) {
			this.player.setVolume(currentMasterVolume);
		}
		else {
			this.player.setVolume(volume);
		}
	}
	
	/**
	 * 
	 * @return The current Minecraft's master volume.
	 */
	private int getMinecraftMasterVolume() {
		return (int) (this.soundSystem.getMasterVolume() * 100);
	}
	
	/**
	 * Send a {@link PacketAskRefresh} packet to the server to ask a refresh of the BGM.
	 * It will mainly be used when a client reactivate the mod after disabling it, to keep it updated with the current region's BGM.
	 */
	public void askForRefresh() {
		PacketHandler.INSTANCE.sendToServer(new PacketAskRefresh());
	}
	
	/**
	 * 
	 * @return true is a BGM is currently playing, false if not.
	 */
	public boolean isPlayerActive() {
		return this.trackManager.isActive();
	}
	
	/**
	 * Stops all vanilla Minecraft's music from playing.
	 */
	public void stopMinecraftMusic() {
		if(this.minecraftPlayingSounds != null) {
			Set<ISound> musicsToStop = new HashSet<ISound>();
			Iterator<ISound> it = this.minecraftPlayingSounds.values().iterator();
			while(it.hasNext()) {
				ISound sound = it.next();
				if(sound != null) {
					SoundCategory category = sound.getCategory();
					if(category != null && category == SoundCategory.MUSIC) {
						musicsToStop.add(sound);
					}
				}
			}
			for(ISound music : musicsToStop) {
				this.soundHandler.stopSound(music);
			}
		}
	}
}

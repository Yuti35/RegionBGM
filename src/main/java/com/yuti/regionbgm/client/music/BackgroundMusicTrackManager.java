package com.yuti.regionbgm.client.music;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Nullable;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackState;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Manage the tracks which needs to be played as a BGM.
 * @author Yuti
 *
 */
@SideOnly(Side.CLIENT)
public class BackgroundMusicTrackManager extends AudioEventAdapter {
	
	/**
	 * The player where the music are loaded.
	 */
	private AudioPlayer player;
	
	/**
	 * Tells if the music (or the playlist) should loop or not
	 */
	private boolean loop = true;
	
	/**
	 * The resource currently playing (an URL, youtube, twitch, almost anything)
	 */
	@Nullable
	private String currentLoadedResource;
	
	/**
	 * The queue of the music which needs to play.
	 * If the resource is a single music, it will contains an element, if it's a playlist, it will contains more.
	 */
	private ConcurrentLinkedQueue<AudioTrack> trackQueue = new ConcurrentLinkedQueue<AudioTrack>();

	public BackgroundMusicTrackManager(AudioPlayer player) {
		this.player = player;
	}
	
	/**
	 * Adds a track to the queue.
	 * @param track The tracks to queue
	 */
	private void queueTrack(AudioTrack track) {
		this.trackQueue.add(track);
	}
	
	/**
	 * Adds a playlist to the queue (queue multiple tracks)
	 * @param playlist The playlist to queue.
	 */
	private void queuePlaylist(AudioPlaylist playlist) {
		for(AudioTrack track : playlist.getTracks()) {
			this.queueTrack(track);
		}
	}
	
	/**
	 * Loads a track by adding it to the queue
	 * @param track The track to load.
	 */
	public void loadAndPlay(AudioTrack track) {
		this.queueTrack(track);
		this.start();
	}
	
	/**
	 * Loads a playlist by adding it to the queue.
	 * @param playlist The playlist to load.
	 */
	public void loadAndPlay(AudioPlaylist playlist) {
		this.queuePlaylist(playlist);
		this.start();
	}
	
	/**
	 * Stops the current resource from playing (and clear the queue)
	 */
	public void stop() {
		this.player.stopTrack();
		this.trackQueue.clear();
		this.setCurrentLoadedResource(null);
	}
	
	/**
	 * Starts the manager from playing the loaded BGM (in the queue).
	 */
	public void start() {
		this.startNextTrack();
	}
	
	/**
	 * Starts the next track in the queue.
	 * If the resource should loops, it will copy and put the track in the queue after polling it.
	 */
	private void startNextTrack() {
		if(!this.trackQueue.isEmpty()) {
			AudioTrack track = this.trackQueue.poll();
			if(this.loop) {
				this.queueTrack(track.makeClone());
			}
			this.player.playTrack(track);
		}
	}
	
	/**
	 * Triggered when the current playing tracks ends.
	 * It will play the next track in the queue.
	 */
	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		super.onTrackEnd(player, track, endReason);
		if(endReason == AudioTrackEndReason.FINISHED) {	
			this.startNextTrack();
		}
	}

	public boolean isLooping() {
		return loop;
	}

	public void setLooping(boolean loop) {
		this.loop = loop;
	}

	@Nullable
	public String getCurrentLoadedResource() {
		return currentLoadedResource;
	}

	public void setCurrentLoadedResource(String currentLoadedResource) {
		this.currentLoadedResource = currentLoadedResource;
	}
	
	/**
	 * Update the current playing resource.
	 * If the resource is identical to the one currently playing, it will keeps on playing it, if not, it will stop the current one to play the new one.
	 * The loop state is updated without influencing the music currently playing (a silent update)
	 * @param resource The resource you want to be played (an URL, youtube, twitch, almost anything)
	 * @param looping Tells is the resource should loop or not
	 * @return true if the resource have been updated (if not identical), false if not. Even if the resource is not updated, the looping will be.
	 * 
	 */
	public boolean updateResource(String resource, boolean looping) {
		if(resource != null) {
			this.setLooping(looping);
			if((this.currentLoadedResource == null || !this.currentLoadedResource .equals(resource) || !this.isActive())) {
				this.stop();
				this.setCurrentLoadedResource(resource);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @return true is a BGM is currently playing, false if not.
	 */
	public boolean isActive() {
		AudioTrack playingTrack = this.player.getPlayingTrack();
		return (playingTrack != null && playingTrack.getState() != AudioTrackState.FINISHED) || !this.trackQueue.isEmpty();
	}
}

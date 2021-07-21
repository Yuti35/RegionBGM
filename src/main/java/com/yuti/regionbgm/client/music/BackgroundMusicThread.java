package com.yuti.regionbgm.client.music;

import static com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats.COMMON_PCM_S16_BE;

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This thread is used to play the BGM of the mod.
 * It will read into the {@link AudioInputStream} provided by the {@link com.sedmelluq.discord.lavaplayer.player.AudioPlayer}
 * Even if it's not playing any sound, the thread it still active and read the audio stream, it will just be silent if no music is playing.
 * @author Yuti
 *
 */
@SideOnly(Side.CLIENT)
public class BackgroundMusicThread extends Thread {
	
	private AudioInputStream stream;
	
	public BackgroundMusicThread(AudioInputStream stream) {
		this.stream = stream;
	}

	@Override
	public void run() {
		super.run();
		SourceDataLine.Info info = new DataLine.Info(SourceDataLine.class, stream.getFormat());
		SourceDataLine line;
		try {
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(stream.getFormat());
			line.start();
			byte[] buffer = new byte[COMMON_PCM_S16_BE.maximumChunkSize()];
			int chunkSize;
			while ((chunkSize = stream.read(buffer)) >= 0) {
				line.write(buffer, 0, chunkSize);
			}
		} catch (LineUnavailableException | IOException e) {

		}
	}
}

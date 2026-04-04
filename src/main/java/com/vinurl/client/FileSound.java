package com.vinurl.client;

import net.minecraft.client.sound.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.vinurl.client.AudioHandler.getAudioFile;
import static com.vinurl.util.Constants.LOGGER;
import static com.vinurl.util.Constants.PLACEHOLDER_SOUND_ID;

public class FileSound extends AbstractSoundInstance {
	public final String fileName;
	public final long startTicks;
	public final long startTimeMillis;

	public FileSound(String fileName, Vec3d position, long ticks, boolean loop) {
		super(PLACEHOLDER_SOUND_ID, SoundCategory.RECORDS, SoundInstance.createRandom());
		this.startTicks = ticks;
		this.startTimeMillis = System.currentTimeMillis();
		this.fileName = fileName;
		this.repeat = loop;
		this.x = position.x;
		this.y = position.y;
		this.z = position.z;
	}

	@Override
	public CompletableFuture<AudioStream> getAudioStream(SoundLoader loader, Identifier id, boolean repeatInstantly) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				InputStream inputStream = new FileInputStream(getAudioFile(fileName));
				LOGGER.info(String.valueOf(startTicks * 50));
				LOGGER.info(String.valueOf(System.currentTimeMillis() + startTicks * 50 - startTimeMillis));
				return repeatInstantly
					? new RepeatingAudioStream(OggAudioStream::new, inputStream)
					: SkippableAudioStream.createOffsetStream(new OggAudioStream(inputStream), System.currentTimeMillis() + startTicks * 50 - startTimeMillis);
			} catch (IOException e) {
				throw new CompletionException(e);
			}
		}, Util.getDownloadWorkerExecutor());
	}
}
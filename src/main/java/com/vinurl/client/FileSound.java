package com.vinurl.client;

import net.minecraft.Util;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.JOrbisAudioStream;
import net.minecraft.client.sounds.LoopingAudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.vinurl.client.SoundManager.getAudioFile;
import static com.vinurl.util.Constants.PLACEHOLDER_SOUND_ID;

public class FileSound extends AbstractSoundInstance {
	public final String fileName;

	public FileSound(String fileName, BlockPos pos, boolean loop) {
		super(PLACEHOLDER_SOUND_ID, SoundSource.RECORDS, SoundInstance.createUnseededRandom());
		this.fileName = fileName;
		this.looping = loop;
		this.x = pos.getCenter().x;
		this.y = pos.getCenter().y;
		this.z = pos.getCenter().z;
	}

	@Override
	public CompletableFuture<AudioStream> getAudioStream(SoundBufferLibrary loader, ResourceLocation id, boolean repeatInstantly) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				InputStream inputStream = new FileInputStream(getAudioFile(fileName));
				return repeatInstantly
					? new LoopingAudioStream(JOrbisAudioStream::new, inputStream)
					: new JOrbisAudioStream(inputStream);
			} catch (IOException e) {
				throw new CompletionException(e);
			}
		}, Util.nonCriticalIoPool());
	}
}
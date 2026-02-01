package com.vinurl.client;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.JOrbisAudioStream;
import net.minecraft.client.sounds.LoopingAudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.vinurl.util.Constants.PLACEHOLDER_SOUND_ID;

public class FileSound extends AbstractSoundInstance {
	public final String fileName;

	public FileSound(String fileName, @Nullable BlockPos pos, boolean loop) {
		super(PLACEHOLDER_SOUND_ID, SoundSource.RECORDS, SoundInstance.createUnseededRandom());
		this.fileName = fileName;
		this.looping = loop;
		if (pos != null) {
			this.x = pos.getCenter().x;
			this.y = pos.getCenter().y;
			this.z = pos.getCenter().z;
		}
		else {
			this.attenuation = Attenuation.NONE;
			this.relative = true;
		}
	}

	@Override
	public CompletableFuture<AudioStream> getAudioStream(SoundBufferLibrary loader, Identifier id, boolean loop) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				InputStream inputStream = new FileInputStream(SoundManager.getAudioFile(fileName));
				return loop
					? new LoopingAudioStream(JOrbisAudioStream::new, inputStream)
					: new JOrbisAudioStream(inputStream);
			} catch (IOException e) {
				throw new CompletionException(e);
			}
		}, Util.nonCriticalIoPool());
	}
}
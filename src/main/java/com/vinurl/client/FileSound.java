package com.vinurl.client;

import net.minecraft.Util;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.JOrbisAudioStream;
import net.minecraft.client.sounds.LoopingAudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.vinurl.VinURL.PLACEHOLDER_SOUND;

public class FileSound extends AbstractTickableSoundInstance {
	public final String fileName;

	public final @Nullable BlockPos position;
	public final @Nullable Entity entity;

	public FileSound(String fileName, BlockPos pos, Entity entity, boolean loop) {
		super(PLACEHOLDER_SOUND, SoundSource.RECORDS, SoundInstance.createUnseededRandom());
		this.fileName = fileName;
		this.position = pos;
		this.entity = entity;
		this.looping = loop;
	}

	@Override
	public void tick() {
		if (entity != null) {
			this.x = entity.getX();
			this.y = entity.getY();
			this.z = entity.getZ();

			if (entity.isRemoved()) {
				this.stop();
			}
		}

		else if (position != null) {
			this.x = position.getCenter().x;
			this.y = position.getCenter().y;
			this.z = position.getCenter().z;
		}
	}

	@Override
	public CompletableFuture<AudioStream> getAudioStream(SoundBufferLibrary loader, ResourceLocation id, boolean loop) {
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
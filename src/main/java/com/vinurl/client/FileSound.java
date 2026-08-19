package com.vinurl.client;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.JOrbisAudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Util;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.vinurl.VinURL.PLACEHOLDER_SOUND;

public class FileSound extends AbstractTickableSoundInstance {
	public final String fileName;
	private final long startTime;

	public final @Nullable BlockPos position;
	public final @Nullable Entity entity;

	public FileSound(String fileName, BlockPos pos, Entity entity) {
		super(PLACEHOLDER_SOUND, SoundSource.RECORDS, SoundInstance.createUnseededRandom());
		this.fileName = fileName;
		this.startTime = Util.getMillis();
		this.position = pos;
		this.entity = entity;
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
			this.x = Vec3.atCenterOf(position).x();
			this.y = Vec3.atCenterOf(position).y();
			this.z = Vec3.atCenterOf(position).z();
		}
	}

	@Override
	public CompletableFuture<AudioStream> getAudioStream(SoundBufferLibrary loader, Identifier id, boolean loop) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				InputStream inputStream = new FileInputStream(SoundManager.getAudioFile(fileName));
				return SkippableAudioStream.offset(new JOrbisAudioStream(inputStream), Util.getMillis() - startTime);
			} catch (IOException e) {
				throw new CompletionException(e);
			}
		}, Util.nonCriticalIoPool());
	}
}
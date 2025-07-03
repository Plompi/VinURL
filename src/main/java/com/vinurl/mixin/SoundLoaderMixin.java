package com.vinurl.mixin;

import com.vinurl.client.AudioHandler;
import com.vinurl.client.SkippableAudioStream;
import net.minecraft.client.sound.AudioStream;
import net.minecraft.client.sound.OggAudioStream;
import net.minecraft.client.sound.RepeatingAudioStream;
import net.minecraft.client.sound.SoundLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.vinurl.client.AudioHandler.getAudioFile;
import static com.vinurl.util.Constants.MOD_ID;

@Mixin(SoundLoader.class)
public class SoundLoaderMixin {
	@Inject(at = @At("HEAD"), method = "loadStreamed", cancellable = true)
	public void loadStreamed(Identifier id, boolean repeatInstantly, CallbackInfoReturnable<CompletableFuture<AudioStream>> cir) {
		if (!id.getNamespace().equals(MOD_ID) || id.getPath().contains("placeholder_sound.ogg")) {return;}

		String[] parts = id.getPath().split("/");
		String fileName = parts[1];
		long skipMilliseconds = Long.parseLong(parts[2].replaceFirst("\\.ogg$", ""));

		cir.setReturnValue(CompletableFuture.supplyAsync(() -> {
			AtomicBoolean first = new AtomicBoolean(true);
			try {
				return repeatInstantly ? new RepeatingAudioStream(
						(in) -> {
							long skip = first.getAndSet(false) ? skipMilliseconds % AudioHandler.getSongDuration(getAudioFile(fileName)) : 0;
							return new SkippableAudioStream(new OggAudioStream(in), skip);
						}, new FileInputStream(getAudioFile(fileName))) : AudioHandler.getAudioInputStream(fileName, skipMilliseconds);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}, Util.getDownloadWorkerExecutor()));
	}
}
package com.vinurl.mixin;

import com.vinurl.client.AudioHandler;
import com.vinurl.client.SkippableAudioStream;
import net.minecraft.client.sound.AudioStream;
import net.minecraft.client.sound.OggAudioStream;
import net.minecraft.client.sound.SoundLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.vinurl.util.Constants.MOD_ID;

@Mixin(SoundLoader.class)
public class SoundLoaderMixin {
	@Inject(at = @At("HEAD"), method = "loadStreamed", cancellable = true)
	public void loadStreamed(Identifier id, boolean repeatInstantly, CallbackInfoReturnable<CompletableFuture<AudioStream>> cir) {
		if (!id.getNamespace().equals(MOD_ID) || id.getPath().contains("placeholder_sound.ogg")) {return;}

		String[] parts = id.getPath().replace(".ogg", "").split("/");
		String fileName = parts[1];
		long skip = System.currentTimeMillis() - Long.parseLong(parts[2]);

		cir.setReturnValue(CompletableFuture.supplyAsync(() -> {
			try {
				return new SkippableAudioStream(new OggAudioStream(AudioHandler.getAudioInputStream(fileName)), skip);
			} catch (IOException e) {
				throw new CompletionException(e);
			}
		}, Util.getDownloadWorkerExecutor()));
	}
}
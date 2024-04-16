package com.vinurl.mixin.client;

import com.vinurl.AudioHandlerClient;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Mixin(SoundLoader.class)
public class SoundLoaderMixin {
	@Inject(at = @At("HEAD"), method = "loadStreamed", cancellable = true)
	public void loadStreamed(Identifier id, boolean repeatInstantly, CallbackInfoReturnable<CompletableFuture<AudioStream>> cir) {
		if (!id.getNamespace().equals("vinurl") || id.getPath().contains("placeholder_sound.ogg"))
			return;

		cir.setReturnValue(CompletableFuture.supplyAsync(() -> {
			try {
				//directly strips out sounds/customsounds (19 chars)
				InputStream inputStream = AudioHandlerClient.getAudioInputStream(id.getPath().substring(19));
				return repeatInstantly ? new RepeatingAudioStream(OggAudioStream::new, inputStream) : new OggAudioStream(inputStream);
			} catch (IOException iOException) {
				throw new CompletionException(iOException);
			}
		}, Util.getMainWorkerExecutor()));

		cir.cancel();
	}
}
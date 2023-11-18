package urlmusicdiscs.mixin.client;

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
import urlmusicdiscs.AudioHandlerClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Mixin(SoundLoader.class)
public class SoundLoaderMixin {
	@Inject(at = @At("HEAD"), method = "loadStreamed", cancellable = true)
	public void loadStreamed(Identifier id, boolean repeatInstantly, CallbackInfoReturnable<CompletableFuture<AudioStream>> cir) {
		if (!id.getNamespace().equals("urlmusicdiscs"))
			return;

		if (id.getPath().contains("placeholder_sound.ogg"))
			return;

		String[] splitNamespace = id.getPath().split("/");
		splitNamespace = Arrays.copyOfRange(splitNamespace, 2, splitNamespace.length);
		String fileUrl = String.join("/", splitNamespace);
		fileUrl = fileUrl.substring(0, fileUrl.length() - 4);

		String finalFileUrl = fileUrl;
		cir.setReturnValue(CompletableFuture.supplyAsync(() -> {
			try {
				AudioHandlerClient audioHandler = new AudioHandlerClient();
				InputStream inputStream = audioHandler.getAudioInputStream(finalFileUrl); //new URL(fileUrl).openStream();

				if (inputStream == null) {
					return null;
				}

				return repeatInstantly ? new RepeatingAudioStream(OggAudioStream::new, inputStream) : new OggAudioStream(inputStream);
			} catch (IOException iOException) {
				throw new CompletionException(iOException);
			}
		}, Util.getMainWorkerExecutor()));

		cir.cancel();
	}
}
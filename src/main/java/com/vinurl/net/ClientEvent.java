package com.vinurl.net;

import com.vinurl.client.KeyListener;
import com.vinurl.client.SoundManager;
import com.vinurl.exe.Executable;
import com.vinurl.gui.URLScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.net.URI;

import static com.vinurl.client.VinURLClient.CONFIG;
import static com.vinurl.util.Constants.NETWORK_CHANNEL;

public class ClientEvent {

	public static void register() {
		// Client event for playing sounds
		NETWORK_CHANNEL.registerClientbound(PlaySoundRecord.class, (payload, context) -> {
			Vec3d position = payload.position().toCenterPos();
			String url = payload.url();
			boolean loop = payload.loop();
			String fileName = SoundManager.getFileName(url);
			MinecraftClient client = context.runtime();

			if (client.player == null || url.isEmpty()) {return;}

			SoundManager.addSound(fileName, position, loop);

			if (Executable.YT_DLP.isProcessRunning(fileName + "/download")) {
				SoundManager.queueSound(fileName, position);
				return;
			}

			if (SoundManager.getAudioFile(fileName).exists()) {
				SoundManager.playSound(position);
				return;
			}

			if (CONFIG.downloadEnabled()) {
				String baseURL = URI.create(url).getScheme() + "://" + URI.create(url).getHost();

				if (CONFIG.urlWhitelist().stream().anyMatch(url::startsWith)) {
					SoundManager.downloadSound(url, fileName);
					SoundManager.queueSound(fileName, position);
					return;
				}

				client.player.sendMessage(
					Text.literal("Press ")
						.append(Text.literal(KeyListener.getHotKey()).formatted(Formatting.YELLOW))
						.append(" to whitelist ")
						.append(Text.literal(baseURL).formatted(Formatting.YELLOW)),
					true
				);

				KeyListener.waitForKeyPress().thenAccept(confirmed -> {
					if (confirmed) {
						CONFIG.urlWhitelist().add(baseURL);
						CONFIG.save();
						SoundManager.downloadSound(url, fileName);
						SoundManager.queueSound(fileName, position);
					}
				});
			}
		});

		// Client event for stopping sounds
		NETWORK_CHANNEL.registerClientbound(StopSoundRecord.class, (payload, context) -> {
			Vec3d position = payload.position().toCenterPos();
			String id = SoundManager.getFileName(payload.url()) + "/download";
			SoundManager.stopSound(position);
			if (Executable.YT_DLP.isProcessRunning(id)) {
				Executable.YT_DLP.getProcessStream(id).unsubscribe(position.toString());
				if (payload.canceled() && Executable.YT_DLP.getProcessStream(id).subscriberCount() <= 1) {
					Executable.YT_DLP.killProcess(id);
				}
			}
		});

		// Client event to open record ui
		NETWORK_CHANNEL.registerClientbound(GUIRecord.class, (payload, context) -> {
			context.runtime().setScreen(new URLScreen(payload.url(), payload.duration(), payload.loop()));
		});
	}

	public record PlaySoundRecord(BlockPos position, String url, boolean loop) {}

	public record StopSoundRecord(BlockPos position, String url, boolean canceled) {}

	public record GUIRecord(String url, int duration, boolean loop) {}
}

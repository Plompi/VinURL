package com.vinurl.client;

import static com.vinurl.VinURL.*;
import com.vinurl.cmd.Commands;
import com.vinurl.exe.FFmpeg;
import com.vinurl.exe.YoutubeDL;
import com.vinurl.gui.URLScreen;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.HashMap;

public class VinURLClient implements ClientModInitializer {
	public static final com.vinurl.client.VinURLConfig CONFIG = com.vinurl.client.VinURLConfig.createAndLoad();
	public static boolean isAprilFoolsDay = LocalDate.now().getMonthValue() == 4 && LocalDate.now().getDayOfMonth() == 1;
	HashMap<Vec3d, FileSound> playingSounds = new HashMap<>();

	@Override
	public void onInitializeClient() {
		// Download FFmpeg and YoutubeDL if they are not already downloaded and checks for updates.
		try {
			FFmpeg.getInstance().checkForExecutable();
			YoutubeDL.getInstance().checkForExecutable();
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}

		Commands.register();

		// Client Music Played Event
		NETWORK_CHANNEL.registerClientbound(PlaySoundRecord.class, (payload, context) -> {
			Vec3d position = payload.position().toCenterPos();
			String url = payload.url();
			String fileName = DigestUtils.sha256Hex(url);
			MinecraftClient client = MinecraftClient.getInstance();
			client.execute(() -> {

				if (client.player == null) {
					return;
				}

				FileSound currentSound = playingSounds.get(position);
				if (currentSound != null) {
					client.getSoundManager().stop(currentSound);
				}

				if (url.isEmpty()){return;}

				if (VinURLClient.CONFIG.DownloadEnabled() && !AudioHandlerClient.fileNameToFile(fileName + ".ogg").exists()) {
					client.player.sendMessage(Text.literal("Downloading music, please wait a moment..."));

					AudioHandlerClient.downloadAudio(url, fileName).thenAccept((result) -> {
						if (result) {
							client.player.sendMessage(Text.literal("Downloading complete!"));

							FileSound fileSound = new FileSound(fileName, position);
							playingSounds.put(position, fileSound);
							client.getSoundManager().play(fileSound);
						} else {
							client.player.sendMessage(Text.literal("Failed to download music!"));
						}
					});
				} else {
					FileSound fileSound = new FileSound(fileName, position);
					playingSounds.put(position, fileSound);
					client.getSoundManager().play(fileSound);
				}
			});
		});

		// Client Open Record UI Event
		NETWORK_CHANNEL.registerClientbound(GUIRecord.class, (payload, context) -> {
			MinecraftClient.getInstance().setScreen(new URLScreen(payload.url()));
		});
	}
}
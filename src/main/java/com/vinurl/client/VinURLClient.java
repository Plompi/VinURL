package com.vinurl.client;

import static com.vinurl.VinURL.*;

import com.vinurl.cmd.Commands;
import com.vinurl.exe.FFmpeg;
import com.vinurl.exe.YoutubeDL;
import com.vinurl.gui.URLScreen;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;

public class VinURLClient implements ClientModInitializer {
	public static final com.vinurl.client.VinURLConfig CONFIG = com.vinurl.client.VinURLConfig.createAndLoad();
	public static Boolean isAprilFoolsDay = LocalDate.now().getMonthValue() == 4 && LocalDate.now().getDayOfMonth() == 1;

	@Override
	public void onInitializeClient() {
		KeyPressListener.init();
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
			Boolean loop = payload.loop();
			String fileName = DigestUtils.sha256Hex(url);
			MinecraftClient client = MinecraftClient.getInstance();
			client.execute(() -> {

				if (client.player == null) {return;}

				FileSound currentSound = AudioHandlerClient.playingSounds.get(position);
				if (currentSound != null) {
					client.getSoundManager().stop(currentSound);
				}

				if (url.isEmpty()) {return;}

				if (VinURLClient.CONFIG.DownloadEnabled() && !AudioHandlerClient.fileNameToFile(fileName + ".ogg").exists()) {

					List<String> whitelist = CONFIG.urlWhitelist();
					String baseURL = AudioHandlerClient.BaseURL(url);

					if (whitelist.stream().noneMatch(url::startsWith)) {
						client.player.sendMessage(
								Text.literal("Press ")
										.append(KeyPressListener.acceptKey.getBoundKeyLocalizedText().copy().formatted(Formatting.YELLOW))
										.append(Text.literal(" to whitelist "))
										.append(Text.literal(baseURL).formatted(Formatting.YELLOW)),
								true
						);

						KeyPressListener.waitForKeyPress().thenAccept(confirmed -> {
							if (confirmed) {
								AudioHandlerClient.downloadAudio(client, url, fileName, position, loop);

								if (!whitelist.contains(baseURL)) {
									whitelist.add(baseURL);
									CONFIG.save();
								}
							}
						});
					}
					else {AudioHandlerClient.downloadAudio(client, url, fileName, position, loop);}
				}
				else {AudioHandlerClient.playSound(client, fileName, position, loop);}
			});
		});

		// Client Open Record UI Event
		NETWORK_CHANNEL.registerClientbound(GUIRecord.class, (payload, context) -> {
			MinecraftClient.getInstance().setScreen(new URLScreen(payload.url(), payload.loop()));
		});
	}
}
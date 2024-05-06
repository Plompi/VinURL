package com.vinurl;

import com.vinurl.cmd.Commands;
import com.vinurl.exe.FFmpeg;
import com.vinurl.exe.YoutubeDL;
import com.vinurl.gui.MusicDiscScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.HashMap;

public class VinURLClient implements ClientModInitializer {
	public static final com.vinurl.VinURLConfig CONFIG = com.vinurl.VinURLConfig.createAndLoad();
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
		ClientPlayNetworking.registerGlobalReceiver(VinURL.PlaySoundPayload.ID, (payload, context) -> {
			Vec3d blockPosition = payload.blockPos().toCenterPos();
			String fileUrl = payload.urlName();
			String fileName = DigestUtils.sha256Hex(fileUrl);
			MinecraftClient client = context.client();
			client.execute(() -> {

				FileSound currentSound = playingSounds.get(blockPosition);

				if (currentSound != null) {
					client.getSoundManager().stop(currentSound);
				}

				if (fileUrl.isEmpty()) {
					return;
				}

				if (!AudioHandlerClient.fileNameToFile(fileName + ".ogg").exists() && client.player != null) {
					client.player.sendMessage(Text.literal("Downloading music, please wait a moment..."));

					AudioHandlerClient.downloadAudio(fileUrl, fileName).thenAccept((result) -> {
						if (result) {
							client.player.sendMessage(Text.literal("Downloading complete!"));

							FileSound fileSound = new FileSound(fileName, blockPosition);
							playingSounds.put(blockPosition, fileSound);
							client.getSoundManager().play(fileSound);
						} else {
							client.player.sendMessage(Text.literal("Failed to download music!"));
						}
					});
				} else {
					FileSound fileSound = new FileSound(fileName, blockPosition);
					playingSounds.put(blockPosition, fileSound);
					client.getSoundManager().play(fileSound);
				}
			});
		});

		// Client Open Record UI Event
		ClientPlayNetworking.registerGlobalReceiver(VinURL.RecordGUIPayload.ID, (payload, context) -> {
			String currentUrl = payload.urlName();
			MinecraftClient client = context.client();
			client.execute(() -> {

				client.setScreen(new MusicDiscScreen(currentUrl));
			});

		});
	}
}
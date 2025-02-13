package com.vinurl.client;

import static com.vinurl.VinURL.*;
import com.vinurl.exe.YoutubeDL;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class AudioHandlerClient {
	static HashMap<Vec3d, FileSound> playingSounds = new HashMap<>();


	public static void downloadAudio(MinecraftClient client, String url, String fileName, Vec3d position, boolean loop) {
		if (client.player == null) {
			return;
		}
		client.player.sendMessage(Text.literal("Downloading music, please wait a moment..."), true);

		// Asynchroner Download
		CompletableFuture.supplyAsync(() -> {
			return YoutubeDL.getInstance().executeCommand(
					url,
					"-x", "--no-progress", "--no-playlist",
					"--break-match-filter", "ext~=3gp|aac|flv|m4a|mov|mp3|mp4|ogg|wav|webm|opus",
					"--audio-format", "vorbis",
					"--audio-quality", VinURLClient.CONFIG.AudioBitrate().getValue(),
					"--postprocessor-args", String.format("ffmpeg:-ac 1 -t %s", VinURLClient.CONFIG.MaxAudioInMinutes() * 60),
					"--ffmpeg-location", VINURLPATH.resolve("ffmpeg").toString(),
					"-o", fileNameToFile(fileName).toString()
			);
		}).thenAccept((result) -> {
			if (result) {
				client.player.sendMessage(Text.literal("Downloading complete!").formatted(Formatting.GREEN), true);
				playSound(client, fileName, position, loop);

			} else {
				client.player.sendMessage(Text.literal("Failed to download music!").formatted(Formatting.RED), true);
			}
		});
	}

	public static void playSound(MinecraftClient client, String fileName, Vec3d position, boolean loop) {
		FileSound fileSound = new FileSound(fileName, position, loop);
		AudioHandlerClient.playingSounds.put(position, fileSound);
		client.getSoundManager().play(fileSound);
	}

	public static InputStream getAudioInputStream(String fileName) {
		try {
			return new FileInputStream(fileNameToFile(fileName));
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	public static File fileNameToFile(String fileName) {
		return new File(VINURLPATH.resolve("client_downloads/" + fileName).toString());
	}

	public static String BaseURL(String url) {
		try {
			URI baseURL = new URI(url);
			return baseURL.getScheme() + "://" + baseURL.getHost();
		} catch (Exception e) {
			return "";
		}
	}
}
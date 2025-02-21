package com.vinurl.client;

import com.vinurl.exe.YoutubeDL;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.codec.digest.DigestUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.vinurl.VinURL.VINURLPATH;

public class AudioHandlerClient {
	static HashMap<Vec3d, FileSound> playingSounds = new HashMap<>();
	static ConcurrentHashMap<String, String> descriptionCache = new ConcurrentHashMap<>();


	public static void downloadSound(MinecraftClient client, String url, String fileName, Vec3d position, boolean loop) {
		if (client.player == null) {
			return;
		}
		client.player.sendMessage(Text.literal("Downloading music, please wait a moment..."), true);

		// Asynchroner Download
		CompletableFuture.supplyAsync(() -> {
			return YoutubeDL.getInstance().executeCommand(
					url,
					"-x", "--no-progress", "--no-playlist", "--add-metadata",
					"--break-match-filter", "ext~=3gp|aac|flv|m4a|mov|mp3|mp4|ogg|wav|webm|opus",
					"--audio-format", "vorbis",
					"--audio-quality", VinURLClient.CONFIG.AudioBitrate().getValue(),
					"--postprocessor-args", String.format("ffmpeg:-ac 1 -t %s", VinURLClient.CONFIG.MaxAudioInMinutes() * 60),
					"--ffmpeg-location", VINURLPATH.resolve("ffmpeg").toString(),
					"-o", fileNameToFile(fileName).toString()
			);
		}).thenAccept((result) -> {
			if (result.success()) {
				client.player.sendMessage(Text.literal("Downloading complete!").formatted(Formatting.GREEN), true);
				playSound(client, fileName, position, loop);
				cacheDescription(url);

			} else {
				client.player.sendMessage(Text.literal("Failed to download music!").formatted(Formatting.RED), true);
			}
		});
	}

	public static void playSound(MinecraftClient client, String fileName, Vec3d position, boolean loop) {
		FileSound fileSound = new FileSound(fileName, position, loop);
		AudioHandlerClient.playingSounds.put(position, fileSound);
		client.getSoundManager().play(fileSound);
		getDescription(fileName).thenAccept(description -> {client.inGameHud.setRecordPlayingOverlay(Text.literal(description));});
	}

	public static CompletableFuture<String> getDescription(String fileName){
		return CompletableFuture.supplyAsync(() -> {
			try {
				File file = new File(fileNameToFile(fileName) + ".ogg");
				AudioFile audioFile = AudioFileIO.read(file);
				Tag tag = audioFile.getTag();

				String artist = tag.getFirst(FieldKey.ARTIST);
				String title = tag.getFirst(FieldKey.TITLE);

				return (artist != null && !artist.isEmpty() && title != null && !title.isEmpty())
						? (artist + " - " + title).replaceAll("[︀-️]", "")
						: "Unknown";
			} catch (Exception e) {
				return "Unknown";
			}
		});
	}

	public static void cacheDescription(String url) {
		CompletableFuture.runAsync(() -> {
			AudioHandlerClient.getDescription(DigestUtils.sha256Hex(url)).thenAccept(description -> {
				descriptionCache.put(url, description);
			});
		});
	}

	public static InputStream getAudioInputStream(String fileName) {
		try {
			return new FileInputStream(fileNameToFile(fileName));
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	public static String getBaseURL(String url) {
		try {
			URI baseURL = new URI(url);
			return baseURL.getScheme() + "://" + baseURL.getHost();
		} catch (Exception e) {
			return "";
		}
	}

	public static File fileNameToFile(String fileName) {
		return new File(VINURLPATH.resolve("client_downloads/" + fileName).toString());
	}
}
package com.vinurl.client;

import com.jcraft.jorbis.JOrbisException;
import com.jcraft.jorbis.VorbisFile;
import com.vinurl.exe.Executable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.vinurl.util.Constants.LOGGER;
import static com.vinurl.util.Constants.VINURLPATH;

public class AudioHandler {
	public static final Path AUDIO_DIRECTORY = VINURLPATH.resolve("downloads");
	static ConcurrentHashMap<Vec3d, FileSound> playingSounds = new ConcurrentHashMap<>();
	static ConcurrentHashMap<String, String> descriptionCache = new ConcurrentHashMap<>();

	public static void downloadSound(MinecraftClient client, String url, String fileName, Vec3d position, boolean loop) {
		if (client.player == null) {
			return;
		}
		client.player.sendMessage(Text.literal("Downloading music, please wait a moment..."), true);

		// Asynchroner Download
		CompletableFuture.supplyAsync(() -> {
			return Executable.YT_DLP.executeCommand(
					url,
					"-x", "-q", "--no-progress", "--concat-playlist", "always", "--add-metadata",
					"-P", AUDIO_DIRECTORY.toString(),
					"--break-match-filter", "ext~=3gp|aac|flv|m4a|mov|mp3|mp4|ogg|wav|webm|opus",
					"--audio-format", "vorbis",
					"--audio-quality", VinURLClient.CONFIG.AudioBitrate().getValue(),
					"--postprocessor-args", String.format("ffmpeg:-ac 1 -t %s", VinURLClient.CONFIG.MaxAudioInMinutes() * 60),
					"--ffmpeg-location", Executable.FFMPEG.DIRECTORY.toString(),
					"-o", String.format("%%(playlist_autonumber&{}|)s%s.%%(ext)s", fileName)
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
		AudioHandler.playingSounds.put(position, fileSound);
		client.getSoundManager().play(fileSound);
		client.inGameHud.setRecordPlayingOverlay(Text.literal(getDescription(fileName)));
	}

	public static String getDescription(String fileName){
		String artist = getOggAttribute(fileName, "artist");
		String title = getOggAttribute(fileName, "title");
		return (artist + " - " + title).replaceAll("[︀-️]", "");
	}

	private static String getOggAttribute(String fileName, String attribute) {
		VorbisFile vorbisFile = null;
		try{
			vorbisFile = new VorbisFile(fileNameToFile(fileName) + ".ogg");
			String metadata = vorbisFile.getComment(0).toString();

			String filter = "Comment: " + attribute + "=";
			return Arrays.stream(metadata.split("\n"))
					.filter(line -> line.startsWith(filter))
					.map(line -> line.substring(filter.length()))
					.findFirst()
					.orElse("N/A");
		} catch (JOrbisException e) {
			return "N/A";
		}
		finally {
			if (vorbisFile != null){
				try {
					vorbisFile.close();
				} catch (IOException e) {
					LOGGER.error("Error closing vorbis file", e);
				}
			}
		}
	}

	public static void cacheDescription(String url) {
		descriptionCache.put(url,getDescription(DigestUtils.sha256Hex(url)));
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
		return new File(AUDIO_DIRECTORY.resolve(fileName).toString());
	}
}
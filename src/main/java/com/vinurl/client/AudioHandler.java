package com.vinurl.client;

import com.jcraft.jorbis.JOrbisException;
import com.jcraft.jorbis.VorbisFile;
import com.vinurl.exe.Executable;
import com.vinurl.gui.ProgressOverlay;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static com.vinurl.client.VinURLClient.CLIENT;
import static com.vinurl.util.Constants.LOGGER;
import static com.vinurl.util.Constants.VINURLPATH;

public class AudioHandler {
	public static final Path AUDIO_DIRECTORY = VINURLPATH.resolve("downloads");
	private static final ConcurrentHashMap<Vec3d, FileSound> playingSounds = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, String> descriptionCache = new ConcurrentHashMap<>();

	public static void downloadSound(String url, String fileName, Vec3d position, boolean loop) {
		if (CLIENT.player == null) {return;}

		CLIENT.player.sendMessage(Text.literal("Downloading music, please wait a moment..."), true);

		Executable.YT_DLP.executeCommand(
			fileName,
			url, "-x", "-q", "--progress", "--concat-playlist", "always", "--add-metadata",
			"--progress-template", "%(info.playlist_index|1)d/%(info.playlist_count|1)d:%(progress._percent)d",
			"--break-match-filter", "ext~=3gp|aac|flv|m4a|mov|mp3|mp4|ogg|wav|webm|opus",
			"--audio-format", "vorbis", "--audio-quality", VinURLClient.CONFIG.audioBitrate().getValue(),
			"--postprocessor-args", String.format("ffmpeg:-ac 1 -t %d", VinURLClient.CONFIG.maxAudioInMinutes() * 60),
			"-P", AUDIO_DIRECTORY.toString(), "--ffmpeg-location", Executable.FFMPEG.DIRECTORY.toString(),
			"-o", String.format("%%(playlist_autonumber&{}|)s%s.%%(ext)s", fileName)
		).subscribe(
			line -> {
				if (line.trim().isEmpty()) {return;}
				ProgressOverlay.set(line.split(":")[0], Integer.parseInt((line.split(":")[1])));
			},
			error -> {
				ProgressOverlay.stop();
				deleteSound(fileName);
				CLIENT.player.sendMessage(Text.literal("Failed to download music!").formatted(Formatting.RED), true);
			},
			() -> {
				ProgressOverlay.stop();
				playSound(fileName, position, loop);
				descriptionToCache(fileName);
			}
		);
	}

	public static void deleteSound(String fileName) {
		File[] filesToDelete = AUDIO_DIRECTORY.toFile().listFiles(file -> file.isFile() && file.getName().contains(fileName));
		if (filesToDelete == null) {return;}

		for (File file : filesToDelete) {
			FileUtils.deleteQuietly(file);
		}
	}

	public static void playSound(String fileName, Vec3d position, boolean loop) {
		FileSound fileSound = new FileSound(fileName, position, loop);
		playingSounds.put(position, fileSound);
		CLIENT.getSoundManager().play(fileSound);
		CLIENT.inGameHud.setRecordPlayingOverlay(Text.literal(getDescription(fileName)));
	}

	public static void stopSound(Vec3d position) {
		CLIENT.getSoundManager().stop(playingSounds.remove(position));
	}

	public static String getDescription(String fileName){
		String artist = getOggAttribute(fileName, "artist");
		String title = getOggAttribute(fileName, "title");
		return (artist + " - " + title).replaceAll("[︀-️]", "");
	}

	private static String getOggAttribute(String fileName, String attribute) {
		VorbisFile vorbisFile = null;
		try{
			vorbisFile = new VorbisFile(getAudioFile(fileName).toString());
			String metadata = vorbisFile.getComment(0).toString();

			String filter = "Comment: " + attribute + "=";
			return Stream.of(metadata.split("\n"))
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

	public static void descriptionToCache(String fileName) {
		descriptionCache.put(fileName, getDescription(fileName));
	}

	public static String descriptionFromCache(String fileName){
		return descriptionCache.get(fileName);
	}

	public static InputStream getAudioInputStream(String fileName) {
		try {
			return new FileInputStream(getAudioFile(fileName));
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

	public static File getAudioFile(String fileName) {
		return AUDIO_DIRECTORY.resolve(fileName + ".ogg").toFile();
	}

	public static String hashURL(String url) {
		return (url == null || url.isEmpty()) ? "" : DigestUtils.sha256Hex(url);
	}
}
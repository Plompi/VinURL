package com.vinurl.client;

import com.jcraft.jorbis.JOrbisException;
import com.jcraft.jorbis.VorbisFile;
import com.vinurl.exe.Executable;
import com.vinurl.gui.ProgressOverlay;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static com.vinurl.client.VinURLClient.CLIENT;
import static com.vinurl.util.Constants.LOGGER;
import static com.vinurl.util.Constants.VINURLPATH;

public class AudioHandler {
	public static final Path AUDIO_DIRECTORY = VINURLPATH.resolve("downloads");
	private static final ConcurrentHashMap<Vec3d, FileSound> playingSounds = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, String> descriptionCache = new ConcurrentHashMap<>();

	public static void downloadSound(String url, String fileName) {
		if (CLIENT.player == null) {return;}
		ProgressOverlay.set(fileName, 0);

		Executable.YT_DLP.executeCommand(
			fileName + "/download",
			url, "-x", "-q", "--progress", "--add-metadata", "--no-playlist",
			"--progress-template", "%(progress._percent)d", "--newline",
			"--break-match-filter", "ext~=3gp|aac|flv|m4a|mov|mp3|mp4|ogg|wav|webm|opus",
			"--audio-format", "vorbis", "--audio-quality", VinURLClient.CONFIG.audioBitrate().getValue(),
			"--postprocessor-args","ffmpeg:-ac 1 -c:a libvorbis",
			"-P", AUDIO_DIRECTORY.toString(), "--ffmpeg-location", Executable.FFMPEG.DIRECTORY.toString(),
			"-o", fileName + ".%(ext)s"
		).subscribe("main")
			.onOutput(line -> {
				ProgressOverlay.set(fileName, Integer.parseInt(line));
			})
			.onError(error -> {
				ProgressOverlay.stopFailed(fileName);
				deleteSound(fileName);
			})
			.onComplete(() -> {
				ProgressOverlay.stop(fileName);
				descriptionToCache(fileName);
			})
		.start();
	}

	public static void deleteSound(String fileName) {
		File[] filesToDelete = AUDIO_DIRECTORY.toFile().listFiles(file -> file.getName().contains(fileName));
		if (filesToDelete == null) {return;}

		for (File file : filesToDelete) {
			FileUtils.deleteQuietly(file);
		}
	}

	public static void playSound(Vec3d position) {
		FileSound fileSound = playingSounds.get(position);
		if (fileSound != null) {
			CLIENT.getSoundManager().play(fileSound);
			CLIENT.inGameHud.setRecordPlayingOverlay(Text.literal(getDescription(fileSound.getId().getPath())));
		}
	}

	public static void stopSound(Vec3d position) {
		CLIENT.getSoundManager().stop(playingSounds.remove(position));
	}

	public static void addSound(String fileName, Vec3d position, boolean loop) {
		playingSounds.put(position, new FileSound(fileName, position, loop));
	}

	public static void queueSound(String fileName, Vec3d position) {
		Executable.YT_DLP.getProcessStream(fileName + "/download").subscribe(position.toString())
			.onComplete(() -> {playSound(position);}).start();
	}

	public static String getDescription(String fileName) {
		return Optional.ofNullable(descriptionFromCache(fileName)).orElseGet(() -> descriptionToCache(fileName));
	}

	private static String getOggAttribute(String fileName, String attribute) {
		VorbisFile vorbisFile = null;
		try {
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
		} finally {
			if (vorbisFile != null) {
				try {
					vorbisFile.close();
				} catch (IOException e) {
					LOGGER.error("Error closing vorbis file", e);
				}
			}
		}
	}

	public static String descriptionToCache(String fileName) {
		return descriptionCache.compute(fileName, (k, v) -> {
			String artist = getOggAttribute(fileName, "artist");
			String title = getOggAttribute(fileName, "title");
			return (artist + " - " + title).replaceAll("[︀-️]", "");
		});
	}

	public static String descriptionFromCache(String fileName) {
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
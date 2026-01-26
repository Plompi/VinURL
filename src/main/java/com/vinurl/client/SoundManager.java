package com.vinurl.client;

import com.jcraft.jorbis.JOrbisException;
import com.jcraft.jorbis.VorbisFile;
import com.vinurl.exe.Executable;
import com.vinurl.gui.ProgressOverlay;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.vinurl.client.VinURLClient.CLIENT;
import static com.vinurl.util.Constants.LOGGER;
import static com.vinurl.util.Constants.VINURLPATH;

public class SoundManager {
	public static final Path AUDIO_DIRECTORY = VINURLPATH.resolve("downloads");
	private static final HashMap<BlockPos, FileSound> playingSounds = new HashMap<>();
	private static final HashMap<String, String> descriptionCache = new HashMap<>();

	public static void downloadSound(String url, String fileName) {
		ProgressOverlay.set(fileName, 0);

		Executable.YT_DLP.executeCommand(
			fileName + "/download",
			url, "-x", "-q", "--progress", "--add-metadata", "--no-playlist",
			"--progress-template", "PROGRESS: %(progress._percent)d", "--newline",
			"--break-match-filter", "ext~=3gp|aac|flv|m4a|mov|mp3|mp4|ogg|wav|webm|opus",
			"--audio-format", "vorbis", "--audio-quality", VinURLClient.CONFIG.audioBitrate().getValue(),
			"--postprocessor-args", "ffmpeg:-ac 1 -c:a libvorbis",
			"-P", AUDIO_DIRECTORY.toString(), "--ffmpeg-location", Executable.FFMPEG.DIRECTORY.toString(),
			"-o", fileName + ".%(ext)s"
		).subscribe("main")
			.onOutput((output) -> {
				String type = output.substring(0, output.indexOf(':') + 1);
				String message = output.substring(type.length()).trim();

				switch (type) {
					case "PROGRESS:" -> ProgressOverlay.set(fileName, Integer.parseInt(message));
					case "WARNING:" -> LOGGER.warn(message);
					case "ERROR:" -> LOGGER.error(message);
					default -> LOGGER.info(output);
				}
			})
			.onError((error) -> {
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

	public static void addSound(String fileName, @Nullable BlockPos pos, boolean loop) {
		FileSound fileSound = playingSounds.put(pos, new FileSound(fileName, pos, loop));
		if (fileSound != null) {
			CLIENT.getSoundManager().stop(fileSound);
		}
	}

	public static void playSound(@Nullable BlockPos pos) {
		FileSound fileSound = playingSounds.get(pos);
		if (fileSound != null) {
			CLIENT.getSoundManager().play(fileSound);
			CLIENT.gui.setNowPlaying(Component.literal(getDescription(fileSound.fileName)));
		}
	}

	public static void stopSound(@Nullable BlockPos pos) {
		FileSound fileSound = playingSounds.remove(pos);
		if (fileSound != null) {
			CLIENT.getSoundManager().stop(fileSound);
		}
	}

	public static void queueSound(String fileName, @Nullable BlockPos pos) {
		Executable.ProcessStream processStream = Executable.YT_DLP.getProcessStream(fileName + "/download");
		if (processStream != null) {
			processStream.subscribe(Objects.toString(pos))
				.onComplete(() -> {playSound(pos);}).start();
		}
	}

	public static void unqueueSound(String fileName, @Nullable BlockPos pos, boolean cancel) {
		Executable.ProcessStream processStream = Executable.YT_DLP.getProcessStream(fileName + "/download");
		if (processStream != null) {
			processStream.unsubscribe(Objects.toString(pos));
			if (cancel && processStream.subscriberCount() <= 1) {
				Executable.YT_DLP.killProcess(processStream.getId());
			}
		}
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
				.filter((line) -> line.startsWith(filter))
				.map((line) -> line.substring(filter.length()))
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
		descriptionCache.remove(fileName);
		return descriptionCache.compute(fileName, (k, v) -> {
			String artist = getOggAttribute(fileName, "artist");
			String title = getOggAttribute(fileName, "title");
			return (artist + " - " + title).replaceAll("[︀-️]", "");
		});
	}

	public static String descriptionFromCache(String fileName) {
		return descriptionCache.get(fileName);
	}

	public static File getAudioFile(String fileName) {
		return AUDIO_DIRECTORY.resolve(fileName + ".ogg").toFile();
	}

	public static String getFileName(String url) {
		return (url == null || url.isEmpty()) ? "" : DigestUtils.sha256Hex(url);
	}
}
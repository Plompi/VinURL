package com.vinurl.client;

import com.jcraft.jorbis.JOrbisException;
import com.jcraft.jorbis.VorbisFile;
import com.vinurl.exe.Executable;
import com.vinurl.gui.ProgressOverlay;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Stream;

import static com.vinurl.client.VinURLClient.CLIENT;
import static com.vinurl.util.Constants.LOGGER;
import static com.vinurl.util.Constants.VINURLPATH;

public class SoundManager {
	public static final Path AUDIO_DIRECTORY = VINURLPATH.resolve("downloads");
	private static final HashSet<FileSound> playingSounds = new HashSet<>();
	private static final HashMap<String, String> descriptionCache = new HashMap<>();

	public static void downloadSound(String url, String fileName) {
		ProgressOverlay.set(fileName, 0);

		Executable.YT_DLP.executeCommand(
			fileName + "/download",
			new CommandLine(Executable.YT_DLP.FILE_PATH).addArguments(new String[] {
				url, "-x", "--no-simulate", "-q", "--progress", "--add-metadata", "--no-playlist",
				"--progress-template", "PROGRESS: %(progress._percent)d", "--newline",
				"--break-match-filter", "ext~=3gp|aac|flv|m4a|mov|mp3|mp4|ogg|wav|webm|opus",
				"--audio-format", "vorbis", "--audio-quality", VinURLClient.CONFIG.audioBitrate().getValue(),
				"--postprocessor-args", "ffmpeg:-ac 1 -c:a libvorbis",
				"--ffmpeg-location", Executable.FFMPEG.DIRECTORY.toString(),
				"-P", AUDIO_DIRECTORY.toString(), "-o", fileName + ".%(ext)s"
			}, false).addArguments(VinURLClient.CONFIG.parameters())
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
				setDescription(fileName);
			})
		.start();
	}

	public static void deleteSound(String fileName) {
		FileUtils.listFiles(AUDIO_DIRECTORY.toFile(), TrueFileFilter.INSTANCE, null)
			.stream()
			.filter(f -> f.getName().contains(fileName))
			.forEach(FileUtils::deleteQuietly);
	}

	public static FileSound getSound(BlockPos pos) {
		return playingSounds.stream()
			.filter(s -> Objects.equals(s.position, pos))
			.findFirst()
			.orElse(null);
	}

	public static void playSound(FileSound fileSound) {
		if (fileSound == null) {return;}
		playingSounds.add(fileSound);
		CLIENT.getSoundManager().play(fileSound);
		CLIENT.gui.setNowPlaying(Component.literal(getDescription(fileSound.fileName)));
	}

	public static void stopSound(FileSound fileSound) {
		if (fileSound == null) {return;}
		playingSounds.remove(fileSound);
		CLIENT.getSoundManager().stop(fileSound);
	}

	public static void queueSound(FileSound fileSound) {
		if (fileSound == null) {return;}
		Executable.ProcessStream processStream = Executable.YT_DLP.getProcessStream(fileSound.fileName + "/download");
		if (processStream != null) {
			processStream.subscribe(Objects.toString(fileSound.position))
				.onComplete(() -> playSound(fileSound)).start();
		}
	}

	public static void unqueueSound(FileSound fileSound, boolean cancel) {
		if (fileSound == null) {return;}
		Executable.ProcessStream processStream = Executable.YT_DLP.getProcessStream(fileSound.fileName + "/download");
		if (processStream != null) {
			processStream.unsubscribe(Objects.toString(fileSound.position));
			if (cancel && processStream.subscriberCount() <= 1) {
				Executable.YT_DLP.killProcess(processStream.getId());
			}
		}
	}

	public static synchronized String getDescription(String fileName) {
		return descriptionCache.computeIfAbsent(fileName, SoundManager::computeDescription);
	}

	public static synchronized void setDescription(String fileName) {
		descriptionCache.put(fileName, computeDescription(fileName));
	}

	private static String computeDescription(String fileName) {
		return getAttribute(fileName, "artist") + " - " + getAttribute(fileName, "title");
	}

	private static String getAttribute(String fileName, String attribute) {
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

	public static File getAudioFile(String fileName) {
		return AUDIO_DIRECTORY.resolve(fileName + ".ogg").toFile();
	}

	public static String getFileName(String url) {
		return (url == null || url.isEmpty()) ? "" : DigestUtils.sha256Hex(url);
	}
}
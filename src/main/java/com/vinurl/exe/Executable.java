package com.vinurl.exe;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.vinurl.client.VinURLClient.CONFIG;
import static com.vinurl.exe.Platform.PLATFORM;
import static com.vinurl.util.Constants.LOGGER;
import static com.vinurl.util.Constants.VINURLPATH;

public enum Executable {

	YT_DLP("yt-dlp", "yt-dlp/yt-dlp",
		switch (PLATFORM) {
			case WIN_X64 -> "yt-dlp.exe";
			case WIN_ARM64 -> "yt-dlp_arm64.exe";
			case MAC_X64, MAC_ARM64 -> "yt-dlp_macos";
			case LIN_X64 -> "yt-dlp_linux";
			case LIN_ARM64 -> "yt-dlp_linux_aarch64";
			case NOT_SUPPORTED -> null;
		}
	),
	FFPROBE("ffprobe", "Tyrrrz/FFmpegBin",
		switch (PLATFORM) {
			case WIN_X64 -> "ffmpeg-windows-x64.zip";
			case WIN_ARM64 -> "ffmpeg-windows-arm64.zip";
			case MAC_X64 -> "ffmpeg-osx-x64.zip";
			case MAC_ARM64 -> "ffmpeg-osx-arm64.zip";
			case LIN_X64 -> "ffmpeg-linux-x64.zip";
			case LIN_ARM64 -> "ffmpeg-linux-arm64.zip";
			case NOT_SUPPORTED -> null;
		}
	),
	FFMPEG("ffmpeg", "Tyrrrz/FFmpegBin",
		switch (PLATFORM) {
			case WIN_X64 -> "ffmpeg-windows-x64.zip";
			case WIN_ARM64 -> "ffmpeg-windows-arm64.zip";
			case MAC_X64 -> "ffmpeg-osx-x64.zip";
			case MAC_ARM64 -> "ffmpeg-osx-arm64.zip";
			case LIN_X64 -> "ffmpeg-linux-x64.zip";
			case LIN_ARM64 -> "ffmpeg-linux-arm64.zip";
			case NOT_SUPPORTED -> null;
		}
	),
	DENO("deno", "denoland/deno",
		switch (PLATFORM) {
			case WIN_X64 -> "deno-x86_64-pc-windows-msvc.zip";
			case WIN_ARM64 -> "deno-aarch64-pc-windows-msvc.zip";
			case MAC_X64 -> "deno-x86_64-apple-darwin.zip";
			case MAC_ARM64 -> "deno-aarch64-apple-darwin.zip";
			case LIN_X64 -> "deno-x86_64-unknown-linux-gnu.zip";
			case LIN_ARM64 -> "deno-aarch64-unknown-linux-gnu.zip";
			case NOT_SUPPORTED -> null;
		}
	);

	public final Path DIRECTORY = VINURLPATH.resolve("executables");
	public final Path FILE_PATH;
	private final String FILE_NAME;
	private final String REPOSITORY_NAME;
	private final String REPOSITORY_FILE;
	private final Path VERSION_PATH;
	private static final ConcurrentHashMap<String, ProcessStream> activeProcesses = new ConcurrentHashMap<>();

	Executable(String fileName, String repositoryName, String repositoryFile) {
		FILE_NAME = fileName;
		REPOSITORY_NAME = repositoryName;
		REPOSITORY_FILE = repositoryFile;
		FILE_PATH = DIRECTORY.resolve(FILE_NAME + (SystemUtils.IS_OS_WINDOWS ? ".exe" : ""));
		VERSION_PATH = DIRECTORY.resolve(FILE_NAME + ".version");
	}

	public static boolean registerProcess(String id, ProcessStream processStream) {
		return activeProcesses.computeIfAbsent(id, (s) -> {
			processStream.onExit(() -> activeProcesses.remove(id));
			return processStream;
		}) == processStream;
	}

	public static boolean isProcessRunning(String id) {
		return activeProcesses.containsKey(id);
	}

	public static ProcessStream getProcessStream(String id) {
		return activeProcesses.get(id);
	}

	public static void killProcess(String id) {
		ProcessStream stream = activeProcesses.remove(id);
		if (stream == null) {return;}

		Process process = stream.getProcess();
		if (process != null) {
			try {
				process.descendants().forEach((processHandle) -> {
					processHandle.destroyForcibly();
					processHandle.onExit().join();
				});
				process.destroyForcibly();
				process.onExit().join();
			} catch (Exception e) {
				LOGGER.error("Failed to kill process with ID: {}", id, e);
			}
		}
	}

	public static ProcessStream executeCommand(String id, CommandLine command) {
		return new ProcessStream(id, command);
	}

	public static void killAllProcesses() {
		for (String id : Set.copyOf(activeProcesses.keySet())) {
			killProcess(id);
		}
	}

	public boolean checkForExecutable() {
		if (DIRECTORY.toFile().exists() || DIRECTORY.toFile().mkdirs()) {
			if (!FILE_PATH.toFile().exists()) {
				return downloadExecutable();
			} else if (CONFIG.updatesOnStartup) {
				checkForUpdates();
			}
			return true;
		}
		return false;
	}

	public boolean checkForUpdates() {
		return !currentVersion().equals(latestVersion()) && downloadExecutable();
	}

	private boolean downloadExecutable() {
		try (InputStream inputStream = getDownloadInputStream()) {
			if (REPOSITORY_FILE.endsWith(".zip")) {
				try (ZipInputStream zipInput = new ZipInputStream(inputStream)) {
					ZipEntry zipEntry = zipInput.getNextEntry();
					while (zipEntry != null) {
						if (zipEntry.getName().endsWith(FILE_NAME + (SystemUtils.IS_OS_WINDOWS ? ".exe" : ""))) {
							Files.copy(zipInput, FILE_PATH, StandardCopyOption.REPLACE_EXISTING);
							break;
						}
						zipEntry = zipInput.getNextEntry();
					}
				}
			} else {
				Files.copy(inputStream, FILE_PATH, StandardCopyOption.REPLACE_EXISTING);
			}
			if (SystemUtils.IS_OS_UNIX) {
				Runtime.getRuntime().exec(new String[] {"chmod", "+x", FILE_PATH.toString()});
			}
			return writeVersion(latestVersion());
		} catch (Exception e) {
			return false;
		}
	}

	private boolean writeVersion(String version) {
		try {
			Files.writeString(VERSION_PATH, version);
			return true;
		} catch (IOException ignored) {
			return false;
		}
	}

	public String currentVersion() {
		try {
			return Files.readString(VERSION_PATH);
		} catch (IOException e) {
			return "";
		}
	}

	private String latestVersion() {
		String url = "https://api.github.com/repos/%s/releases/latest".formatted(REPOSITORY_NAME);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URI(url).toURL().openStream()))) {
			return reader.readLine().split("\"tag_name\":\"")[1].split("\",\"target_commitish\"")[0];
		} catch (IOException | ArrayIndexOutOfBoundsException | URISyntaxException e) {
			return "";
		}
	}

	private InputStream getDownloadInputStream() throws IOException, URISyntaxException {
		String url = "https://github.com/%s/releases/latest/download/%s".formatted(REPOSITORY_NAME, REPOSITORY_FILE);
		return new URI(url).toURL().openStream();
	}

	public CommandLine getCommandLine() {
		return new CommandLine(FILE_PATH);
	}
}
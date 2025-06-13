package com.vinurl.exe;

import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.vinurl.client.VinURLClient.CONFIG;
import static com.vinurl.util.Constants.LOGGER;
import static com.vinurl.util.Constants.VINURLPATH;

public enum Executable {

	YT_DLP("yt-dlp",
			String.format("yt-dlp%s", (SystemUtils.IS_OS_LINUX ? "_linux" : SystemUtils.IS_OS_MAC ? "_macos" : ".exe")),
			"yt-dlp/yt-dlp"),
	FFPROBE("ffprobe",
			String.format("ffprobe-%s-x64", (SystemUtils.IS_OS_LINUX ? "linux" : SystemUtils.IS_OS_MAC ? "darwin" : "win32")),
			"eugeneware/ffmpeg-static"),
	FFMPEG("ffmpeg",
			String.format("ffmpeg-%s-x64", (SystemUtils.IS_OS_LINUX ? "linux" : SystemUtils.IS_OS_MAC ? "darwin" : "win32")),
			"eugeneware/ffmpeg-static");

	public final Path DIRECTORY = VINURLPATH.resolve("executables");
	private final String FILENAME;
	private final String REPOSITORY_FILE;
	private final String REPOSITORY_NAME;
	private final Path FILEPATH;
	private final Set<Process> activeProcesses = ConcurrentHashMap.newKeySet();

	Executable(String fileName, String repositoryFile, String repositoryName) {
		FILENAME = fileName;
		REPOSITORY_FILE = repositoryFile;
		REPOSITORY_NAME = repositoryName;
		FILEPATH = DIRECTORY.resolve(FILENAME + (SystemUtils.IS_OS_WINDOWS ? ".exe" : ""));
	}

	public void registerProcess(Process process) {
		activeProcesses.add(process);
		process.onExit().thenAccept(activeProcesses::remove);
	}

	public void killAllProcesses() {
		activeProcesses.forEach(process -> {
			try {
				process.descendants().forEach(ph -> {
					ph.destroyForcibly();
					ph.onExit().join();
				});
				process.destroyForcibly();
			} catch (Exception e) {
				LOGGER.error("Failed to kill process", e);
			}
		});
		activeProcesses.clear();
	}

	public void checkForExecutable() throws IOException, URISyntaxException {
		if (DIRECTORY.toFile().exists() || DIRECTORY.toFile().mkdirs()) {
			if (!FILEPATH.toFile().exists()) {
				downloadExecutable();
			} else if (CONFIG.updatesOnStartup()) {
				checkForUpdates();
			}
		}
	}

	public boolean checkForUpdates() {
		try {
			if (!currentVersion(DIRECTORY.resolve(FILENAME + ".version")).equals(latestVersion())) {
				downloadExecutable();
				return true;
			}
			return false;
		} catch (Exception ignored) {
			return false;
		}
	}

	private void downloadExecutable() throws IOException, URISyntaxException {
		try (InputStream inputStream = getDownloadInputStream()) {
			if (REPOSITORY_FILE.endsWith(".zip")) {
				try (ZipInputStream zipInput = new ZipInputStream(inputStream)) {
					ZipEntry zipEntry = zipInput.getNextEntry();
					while (zipEntry != null) {
						if (zipEntry.getName().endsWith(FILENAME + (SystemUtils.IS_OS_WINDOWS ? ".exe" : ""))) {
							Files.copy(zipInput, FILEPATH, StandardCopyOption.REPLACE_EXISTING);
							break;
						}
						zipEntry = zipInput.getNextEntry();
					}
				}
			} else {
				Files.copy(inputStream, FILEPATH, StandardCopyOption.REPLACE_EXISTING);
			}
			if (SystemUtils.IS_OS_UNIX) {
				Runtime.getRuntime().exec(new String[] {"chmod", "+x", FILEPATH.toString()});
			}
			createVersionFile(latestVersion(), DIRECTORY.resolve(FILENAME + ".version"));
		}
	}

	private void createVersionFile(String version, Path versionFilePath) throws IOException {
		try (FileWriter writer = new FileWriter(versionFilePath.toFile())) {
			writer.write(version);
		}
	}

	private String currentVersion(Path filePath) {
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
			String version = reader.readLine();
			return (version != null ? version : "");
		} catch (IOException e) {
			return "";
		}
	}

	private String latestVersion() {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URI(String.format("https://api.github.com/repos/%s/releases/latest", REPOSITORY_NAME)).toURL().openStream()))) {
			return reader.readLine().split("\"tag_name\":\"")[1].split("\",\"target_commitish\"")[0];
		} catch (IOException | ArrayIndexOutOfBoundsException | URISyntaxException e) {
			return "";
		}
	}

	private InputStream getDownloadInputStream() throws IOException, URISyntaxException {
		return new URI(String.format("https://github.com/%s/releases/latest/download/%s", REPOSITORY_NAME, REPOSITORY_FILE)).toURL().openStream();
	}

	public CommandResult executeCommand(String... arguments) {
		StringBuilder output = new StringBuilder();
		boolean success = false;

		try {
			Process process = Runtime.getRuntime().exec(
					Stream.concat(Stream.of(FILEPATH.toString()), Arrays.stream(arguments)).toArray(String[]::new)
			);

			registerProcess(process);

			try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
				for (String line; (line = errorReader.readLine()) != null;) {
					LOGGER.warn(line);
				}
			}

			try (BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				for (String line; (line = outputReader.readLine()) != null;) {
					output.append(line).append("\n");
				}
			}

			if (process.waitFor() == 0) {
				success = true;
			} else {
				LOGGER.error("Command failed with exit code: {}", process.exitValue());
			}
		} catch (IOException | InterruptedException e) {
			LOGGER.error("Failed to execute command: {}", e.getMessage());
		}

		return new CommandResult(success, output.toString().trim());
	}

	public record CommandResult(boolean success, String output) {}
}
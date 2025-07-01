package com.vinurl.exe;

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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.function.Consumer;
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
	private final String FILE_NAME;
	private final String REPOSITORY_FILE;
	private final String REPOSITORY_NAME;
	private final Path VERSION_PATH;
	private final Path FILE_PATH;
	private final ConcurrentHashMap<String, Process> activeProcesses = new ConcurrentHashMap<>();

	Executable(String fileName, String repositoryFile, String repositoryName) {
		FILE_NAME = fileName;
		REPOSITORY_FILE = repositoryFile;
		REPOSITORY_NAME = repositoryName;
		FILE_PATH = DIRECTORY.resolve(FILE_NAME + (SystemUtils.IS_OS_WINDOWS ? ".exe" : ""));
		VERSION_PATH = DIRECTORY.resolve(FILE_NAME + ".version");
	}

	public void registerProcess(String id, Process process) {
		activeProcesses.computeIfAbsent(id, k -> {
			process.onExit().thenAccept(p -> activeProcesses.remove(id));
			return process;
		});
	}

	public void killProcess(String id) {
		Process process = activeProcesses.remove(id);
		if (process != null) {
			try {
				process.descendants().forEach(ph -> {
					ph.destroyForcibly();
					ph.onExit().join();
				});
				process.destroyForcibly();
				process.onExit().join();
			} catch (Exception e) {
				LOGGER.error("Failed to kill process with ID: {} ", id, e);
			}
		}
	}

	public void killAllProcesses() {
		for (String id : Set.copyOf(activeProcesses.keySet())) {
			killProcess(id);
		}
	}

	public boolean checkForExecutable() {
		if (DIRECTORY.toFile().exists() || DIRECTORY.toFile().mkdirs()) {
			if (!FILE_PATH.toFile().exists()) {
				 return downloadExecutable();
			} else if (CONFIG.updatesOnStartup()) {
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
			return createVersionFile(latestVersion());
		} catch (Exception e) {
			return false;
		}
	}

	private boolean createVersionFile(String version) {
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
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URI(String.format("https://api.github.com/repos/%s/releases/latest", REPOSITORY_NAME)).toURL().openStream()))) {
			return reader.readLine().split("\"tag_name\":\"")[1].split("\",\"target_commitish\"")[0];
		} catch (IOException | ArrayIndexOutOfBoundsException | URISyntaxException e) {
			return "";
		}
	}

	private InputStream getDownloadInputStream() throws IOException, URISyntaxException {
		return new URI(String.format("https://github.com/%s/releases/latest/download/%s", REPOSITORY_NAME, REPOSITORY_FILE)).toURL().openStream();
	}

	public ProcessStream executeCommand(String id, String... arguments) {
		return new ProcessStream(id, arguments);
	}

	public class ProcessStream  {
		private final String id;
		private final String[] arguments;
		private final SubmissionPublisher<String> publisher = new SubmissionPublisher<>();

		public ProcessStream(String id, String... arguments) {
			this.id = id;
			this.arguments = arguments;
			CompletableFuture.runAsync(this::startProcess);
		}

		public void subscribe(Consumer<String> onOutput, Consumer<Throwable> onError, Runnable onComplete) {
			publisher.subscribe(new Flow.Subscriber<>() {
				public void onSubscribe(Flow.Subscription s) { s.request(Long.MAX_VALUE); }
				public void onNext(String line) { onOutput.accept(line); }
				public void onError(Throwable t) { onError.accept(t); }
				public void onComplete() { onComplete.run(); }
			});
		}

		private void startProcess() {
			if (activeProcesses.containsKey(id)) {
				publisher.closeExceptionally(new IllegalStateException("Process already running: " + id));
				return;
			}

			try {
				Process process = new ProcessBuilder()
						.command(Stream.concat(Stream.of(FILE_PATH.toString()),
										Stream.of(arguments))
								.toArray(String[]::new)).
						redirectErrorStream(true).start();

				registerProcess(id, process);

				CompletableFuture.runAsync(() -> {
					try (BufferedReader reader = new BufferedReader(
							new InputStreamReader(process.getInputStream()))) {
						String line;
						while ((line = reader.readLine()) != null && !publisher.isClosed()) {
							publisher.submit(line);
						}
					} catch (IOException e) {
						if (!publisher.isClosed()) {
							publisher.closeExceptionally(e);
						}
					}
				});

				process.onExit().thenAccept(p -> {
					if (p.exitValue() == 0) {
						publisher.close();
					} else {
						publisher.closeExceptionally(new IOException("Process failed with code: " + p.exitValue()));
					}
				});

			} catch (IOException e) {
				publisher.closeExceptionally(e);
			}
		}
	}
}
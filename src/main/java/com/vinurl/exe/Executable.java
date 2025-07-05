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

	YT_DLP("yt-dlp", "yt-dlp/yt-dlp",
		String.format("yt-dlp%s", (SystemUtils.IS_OS_LINUX ? "_linux" : SystemUtils.IS_OS_MAC ? "_macos" : ".exe"))),
	FFPROBE("ffprobe", "eugeneware/ffmpeg-static",
		String.format("ffprobe-%s-x64", (SystemUtils.IS_OS_LINUX ? "linux" : SystemUtils.IS_OS_MAC ? "darwin" : "win32"))),
	FFMPEG("ffmpeg", "eugeneware/ffmpeg-static",
		String.format("ffmpeg-%s-x64", (SystemUtils.IS_OS_LINUX ? "linux" : SystemUtils.IS_OS_MAC ? "darwin" : "win32")));

	public final Path DIRECTORY = VINURLPATH.resolve("executables");
	private final String FILE_NAME;
	private final String REPOSITORY_NAME;
	private final String REPOSITORY_FILE;
	private final Path FILE_PATH;
	private final Path VERSION_PATH;
	private final ConcurrentHashMap<String, ProcessStream> activeProcesses = new ConcurrentHashMap<>();

	Executable(String fileName, String repositoryName, String repositoryFile) {
		FILE_NAME = fileName;
		REPOSITORY_NAME = repositoryName;
		REPOSITORY_FILE = repositoryFile;
		FILE_PATH = DIRECTORY.resolve(FILE_NAME + (SystemUtils.IS_OS_WINDOWS ? ".exe" : ""));
		VERSION_PATH = DIRECTORY.resolve(FILE_NAME + ".version");
	}

	public boolean registerProcess(String id, ProcessStream processStream) {
		return activeProcesses.computeIfAbsent(id, k -> {
			processStream.onExit(() -> activeProcesses.remove(id));
			return processStream;
		}) == processStream;
	}

	public boolean isProcessRunning(String id) {
		return activeProcesses.containsKey(id);
	}

	public ProcessStream getProcessStream(String id) {
		return activeProcesses.get(id);
	}

	public void killProcess(String id) {
		ProcessStream stream = activeProcesses.remove(id);
		if (stream != null && stream.process != null) {
			try {
				stream.process.descendants().forEach(ph -> {
					ph.destroyForcibly();
					ph.onExit().join();
				});
				stream.process.destroyForcibly();
				stream.process.onExit().join();
			} catch (Exception e) {
				LOGGER.error("Failed to kill process with ID: {}", id, e);
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
		String url= String.format("https://api.github.com/repos/%s/releases/latest", REPOSITORY_NAME);
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URI(url).toURL().openStream()))) {
			return reader.readLine().split("\"tag_name\":\"")[1].split("\",\"target_commitish\"")[0];
		} catch (IOException | ArrayIndexOutOfBoundsException | URISyntaxException e) {
			return "";
		}
	}

	private InputStream getDownloadInputStream() throws IOException, URISyntaxException {
		String url = String.format("https://github.com/%s/releases/latest/download/%s", REPOSITORY_NAME, REPOSITORY_FILE);
		return new URI(url).toURL().openStream();
	}

	public ProcessStream executeCommand(String id, String... arguments) {
		return new ProcessStream(id, arguments);
	}

	public class ProcessStream {
		private final String id;
		private final String[] arguments;
		private Process process;
		private final SubmissionPublisher<String> publisher = new SubmissionPublisher<>();
		private final ConcurrentHashMap<String, Flow.Subscription> subscriptions = new ConcurrentHashMap<>();

		public ProcessStream(String id, String... arguments) {
			this.id = id;
			this.arguments = arguments;
			if (registerProcess(id, this)) {
				CompletableFuture.runAsync(this::startProcess);
			}
		}

		public SubscriberBuilder subscribe(String subscriberId) {
			return new SubscriberBuilder(subscriberId);
		}

		public class SubscriberBuilder {
			private final String subscriberId;
			private Consumer<String> onOutput = s -> {};
			private Consumer<Throwable> onError = t -> {};
			private Runnable onComplete = () -> {};

			public SubscriberBuilder(String subscriberId) {
				this.subscriberId = subscriberId;
			}

			public SubscriberBuilder onOutput(Consumer<String> consumer) {
				this.onOutput = consumer;
				return this;
			}

			public SubscriberBuilder onError(Consumer<Throwable> consumer) {
				this.onError = consumer;
				return this;
			}

			public SubscriberBuilder onComplete(Runnable runnable) {
				this.onComplete = runnable;
				return this;
			}

			public void start() {
				publisher.subscribe(new Flow.Subscriber<>() {
					@Override
					public void onSubscribe(Flow.Subscription subscription) {
						subscriptions.put(subscriberId, subscription);
						subscription.request(Long.MAX_VALUE);
					}

					@Override
					public void onNext(String item) {
						onOutput.accept(item);
					}

					@Override
					public void onError(Throwable throwable) {
						subscriptions.remove(subscriberId);
						onError.accept(throwable);
					}

					@Override
					public void onComplete() {
						subscriptions.remove(subscriberId);
						onComplete.run();
					}
				});
			}
		}

		public int subscriberCount() {
			return subscriptions.size();
		}

		public void unsubscribe(String subscriberId) {
			Flow.Subscription subscription = subscriptions.remove(subscriberId);
			if (subscription != null) {
				subscription.cancel();
			}
		}

		public void onExit(Runnable callback) {
			if (process != null) {
				process.onExit().thenRun(() -> {
					subscriptions.keySet().forEach(this::unsubscribe);
					callback.run();
				});
			}
		}

		private void startProcess() {
			try {
				process = new ProcessBuilder()
					.command(Stream.concat(Stream.of(FILE_PATH.toString()), Stream.of(arguments)).toArray(String[]::new))
					.redirectErrorStream(true)
					.start();

				try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
					String line;
					while ((line = reader.readLine()) != null && !publisher.isClosed()) {
						publisher.submit(line);
					}
				}

				int exitCode = process.waitFor();

				if (exitCode == 0) {
					publisher.close();
				} else {
					publisher.closeExceptionally(new IOException("Process failed with code: " + exitCode));
				}
			} catch (IOException | InterruptedException e) {
				publisher.closeExceptionally(e);
			} finally {
				killProcess(id);
			}
		}
	}
}
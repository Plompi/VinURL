package com.vinurl.exe;

import com.vinurl.VinURL;
import com.vinurl.VinURLClient;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Executable {
	private final String FILENAME;
	private final File DIRECTORY;
	private final String REPOSITORY_FILE;
	private final String REPOSITORY_NAME;
	private final Path FILEPATH;

	public Executable(String fileName, File directory, String repository_file, String repository_name) {
		FILENAME = fileName;
		DIRECTORY = directory;
		REPOSITORY_FILE = repository_file;
		REPOSITORY_NAME = repository_name;
		FILEPATH = DIRECTORY.toPath().resolve(FILENAME);
	}

	public void checkForExecutable() throws IOException, URISyntaxException {

		if (DIRECTORY.exists() || DIRECTORY.mkdirs()) {
			if (!FILEPATH.toFile().exists()) {
				downloadExecutable();
			} else if (VinURLClient.CONFIG.UpdateCheckingOnStartup()) {
				checkForUpdates();
			}
		}
	}

	public boolean checkForUpdates() {
		try {
			if (!currentVersion(FILEPATH.getParent().resolve("version.txt")).equals(latestVersion())) {
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
						if (zipEntry.getName().endsWith(FILENAME)) {
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
			createVersionFile(latestVersion(), FILEPATH.getParent().resolve("version.txt"));
		}
	}

	private void createVersionFile(String version, Path versionFilePath) throws IOException {
		try (FileWriter writer = new FileWriter(versionFilePath.toFile())) {
			writer.write(version);
		}
	}

	private String currentVersion(Path filePath) throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
			return reader.readLine().trim();
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

	public boolean executeCommand(String... arguments) {
		try {
			Process process = Runtime.getRuntime().exec(Stream.concat(Stream.of(FILEPATH.toString()), Arrays.stream(arguments)).toArray(String[]::new));

			try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
				for (String line; (line = errorReader.readLine()) != null; ) {
					VinURL.LOGGER.info(line);
				}
				if (process.waitFor() != 0) {
					throw new IOException();
				}
			}
			return true;
		} catch (IOException | InterruptedException e) {
			return false;
		}
	}
}
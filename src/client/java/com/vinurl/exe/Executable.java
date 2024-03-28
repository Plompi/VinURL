package com.vinurl.exe;

import com.vinurl.VinURL;
import com.vinurl.VinURLClient;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Executable {

	static void checkForExecutable(String fileName, File directory, String repositoryFile, String repositoryName) throws IOException, URISyntaxException {
		Path filePath = getFilePath(fileName, directory);

		if (directory.exists() || directory.mkdirs()) {
			if (!filePath.toFile().exists()) {
				downloadExecutable(fileName, filePath, repositoryFile, repositoryName);
			} else if (VinURLClient.CONFIG.UpdateCheckingOnStartup()) {
				checkForUpdates(fileName, directory, repositoryFile, repositoryName);
			}
		}
	}

	static boolean checkForUpdates(String fileName, File directory, String repositoryFile, String repositoryName) {
		try {
			Path filePath = getFilePath(fileName, directory);
			if (!currentVersion(filePath.getParent().resolve("version.txt")).equals(latestVersion(repositoryName))) {
				Files.deleteIfExists(filePath);
				downloadExecutable(fileName, filePath, repositoryFile, repositoryName);
				return true;
			}
			return false;
		} catch (Exception ignored) {
			return false;
		}
	}

	private static void downloadExecutable(String fileName, Path filePath, String repositoryFile, String repositoryName) throws IOException, URISyntaxException {
		try (InputStream inputStream = getDownloadInputStream(repositoryFile, repositoryName)) {
			if (repositoryFile.endsWith(".zip")) {
				try (ZipInputStream zipInput = new ZipInputStream(inputStream)) {
					ZipEntry zipEntry = zipInput.getNextEntry();
					while (zipEntry != null) {
						if (zipEntry.getName().endsWith(fileName)) {
							Files.copy(zipInput, filePath, StandardCopyOption.REPLACE_EXISTING);
							break;
						}
						zipEntry = zipInput.getNextEntry();
					}
				}
			} else {
				Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
			}
			if (SystemUtils.IS_OS_UNIX) {
				Runtime.getRuntime().exec(new String[]{"chmod", "+x", filePath.toString()});
			}
			createVersionFile(latestVersion(repositoryName), filePath.getParent().resolve("version.txt"));
		}
	}

	private static void createVersionFile(String version, Path versionFilePath) throws IOException {
		try (FileWriter writer = new FileWriter(versionFilePath.toFile())) {
			writer.write(version);
		}
	}

	private static String currentVersion(Path filePath) throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
			return reader.readLine().trim();
		}
	}

	private static String latestVersion(String repositoryName) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(String.format("https://api.github.com/repos/%s/releases/latest", repositoryName)).openStream()))) {
			return reader.readLine().split("\"tag_name\":\"")[1].split("\",\"target_commitish\"")[0];
		} catch (IOException | ArrayIndexOutOfBoundsException e) {
			return "";
		}
	}

	private static InputStream getDownloadInputStream(String repositoryFile, String repositoryName) throws IOException, URISyntaxException {
		return new URI(String.format("https://github.com/%s/releases/latest/download/%s", repositoryName, repositoryFile)).toURL().openStream();
	}

	static boolean executeCommand(String fileName, File directory, String... arguments) {
		try {
			Process process = Runtime.getRuntime().exec(Stream.concat(Stream.of(getFilePath(fileName, directory).toString()), Arrays.stream(arguments)).toArray(String[]::new));

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

	private static Path getFilePath(String fileName, File directory) {
		return directory.toPath().resolve(fileName);
	}
}
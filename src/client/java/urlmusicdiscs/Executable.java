package urlmusicdiscs;

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

    static void checkForExecutable(String FileName, File Directory, Path FilePath, String RepositoryFile, String RepositoryName) throws IOException, URISyntaxException {
        if(!Directory.exists() && !Directory.mkdirs()) {
            throw new IOException();
        }
        if (!FilePath.toFile().exists()) {downloadExecutable(FileName, FilePath, RepositoryFile, RepositoryName);}
        else if (URLMusicDiscs.CONFIG.currentData.UpdateCheckingOnStartup){
            checkForUpdates(FileName, FilePath, RepositoryFile, RepositoryName);
        }
    }

    static boolean checkForUpdates(String FileName, Path FilePath, String RepositoryFile, String RepositoryName) {
        try{
            if (!currentVersion(FilePath.getParent().resolve("version.txt")).equals(latestVersion(RepositoryName))){
                Files.deleteIfExists(FilePath);
                downloadExecutable(FileName, FilePath, RepositoryFile, RepositoryName);
                return true;
            }
            return false;
        }
        catch (Exception ignored){
            return false;
        }
    }

    static void downloadExecutable(String FileName, Path FilePath, String RepositoryFile, String RepositoryName) throws IOException, URISyntaxException {
        try (InputStream inputStream = getDownloadInputStream(RepositoryFile,RepositoryName)) {
            if (RepositoryFile.endsWith(".zip")) {
                try (ZipInputStream zipInput = new ZipInputStream(inputStream)) {
                    ZipEntry zipEntry = zipInput.getNextEntry();
                    while (zipEntry != null) {
                        if (zipEntry.getName().endsWith(FileName)) {
                            Files.copy(zipInput, FilePath, StandardCopyOption.REPLACE_EXISTING);
                            break;
                        }
                        zipEntry = zipInput.getNextEntry();
                    }
                }
            }
            else {
                Files.copy(inputStream, FilePath, StandardCopyOption.REPLACE_EXISTING);
            }
            if (SystemUtils.IS_OS_UNIX) {
                Runtime.getRuntime().exec(new String[]{"chmod", "+x", FilePath.toString()});
            }
            createVersionFile(latestVersion(RepositoryName), FilePath.getParent().resolve("version.txt"));
        }
    }

    static void createVersionFile(String version, Path versionFilePath) throws IOException {
        try (FileWriter writer = new FileWriter(versionFilePath.toFile())) {
            writer.write(version);
        }
    }

    static String currentVersion(Path FilePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(FilePath.toFile()))) {
            return reader.readLine().trim();
        }
    }

    static String latestVersion(String RepositoryName){
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(String.format("https://api.github.com/repos/%s/releases/latest",RepositoryName)).openStream()))) {
            return reader.readLine().split("\"tag_name\":\"")[1].split("\",\"target_commitish\"")[0];
        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            return "";
        }
    }

    private static InputStream getDownloadInputStream(String RepositoryFile, String RepositoryName) throws IOException, URISyntaxException {
        return new URI(String.format("https://github.com/%s/releases/latest/download/%s",RepositoryName,RepositoryFile)).toURL().openStream();
    }

    static void executeCommand(String executable, String ... arguments) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(Stream.concat(Stream.of(executable),Arrays.stream(arguments)).toArray(String[]::new));
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))){
            for (String line; (line = errorReader.readLine()) != null;) {
                URLMusicDiscs.LOGGER.info(line);
            }
        }
        if (process.waitFor() != 0){throw new IOException();}
    }
}

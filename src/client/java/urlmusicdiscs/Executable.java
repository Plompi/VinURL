package urlmusicdiscs;

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

    static void checkForExecutable(String DownloadURL, String FileName ,File Directory, Path FilePath) throws IOException, URISyntaxException {

        if(!Directory.exists() && !Directory.mkdirs()) {
            throw new IOException();
        }
        if (!FilePath.toFile().exists()) {downloadExecutable(DownloadURL, FileName,FilePath);}
    }

    static void downloadExecutable(String DownloadURL, String FileName, Path FilePath) throws IOException, URISyntaxException {
        try (InputStream inputStream = getDownloadInputStream(DownloadURL)) {
            if (DownloadURL.endsWith(".zip")) {
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
        }
    }

    private static InputStream getDownloadInputStream(String DownloadURL) throws IOException, URISyntaxException {
        return new URI(DownloadURL).toURL().openStream();
    }

    static void executeCommand(String executable, String ... arguments) throws IOException, InterruptedException {

        Process process = Runtime.getRuntime().exec(Stream.concat(Stream.of(executable),Arrays.stream(arguments)).toArray(String[]::new));
        String line;
        while ((line = new BufferedReader(new InputStreamReader(process.getErrorStream())).readLine()) != null){
            URLMusicDiscs.LOGGER.info(line);
        }
        if (process.waitFor() != 0){throw new IOException();}
    }
}

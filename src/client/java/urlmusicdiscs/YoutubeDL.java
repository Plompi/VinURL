package urlmusicdiscs;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.stream.Stream;

public class YoutubeDL {
    private static final String FileName = SystemUtils.IS_OS_LINUX ? "yt-dlp_linux" : SystemUtils.IS_OS_MAC ? "yt-dlp_macos" : "yt-dlp.exe";
    private static final File Directory = FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/youtubedl").toFile();
    private static final Path FilePath = Directory.toPath().resolve(FileName);
    static void checkForExecutable() throws IOException, URISyntaxException {

        if(!Directory.exists() && !Directory.mkdirs()) {
            throw new IOException();
        }

        if (!FilePath.toFile().exists()) {

            try (InputStream in = getDownloadInputStream()) {
                Files.copy(in, FilePath, StandardCopyOption.REPLACE_EXISTING);

                if (SystemUtils.IS_OS_UNIX) {
                    Runtime.getRuntime().exec(new String[]{"chmod", "+x", FilePath.toString()});
                }
            }
        }
    }

    static void executeYoutubeDLCommand(String ... arguments) throws IOException, InterruptedException {
        Runtime.getRuntime().exec(Stream.concat(Stream.of(FilePath.toString()),Arrays.stream(arguments)).toArray(String[]::new)).waitFor();
    }

    private static InputStream getDownloadInputStream() throws IOException, URISyntaxException {
        return new URI(String.format("https://github.com/yt-dlp/yt-dlp/releases/latest/download/%s",FileName)).toURL().openStream();

    }
}

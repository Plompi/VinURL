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
    private static final File Directory = FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/youtubedl").toAbsolutePath().toFile();
    private static final Path FilePath = Directory.toPath().resolve(SystemUtils.IS_OS_WINDOWS ? "yt-dlp.exe" : "yt-dlp");
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
        // https://stackoverflow.com/questions/5711084/java-runtime-getruntime-getting-output-from-executing-a-command-line-program
        Runtime.getRuntime().exec(Stream.concat(Stream.of(FilePath.toString()),Arrays.stream(arguments)).toArray(String[]::new)).waitFor();
    }

    private static InputStream getDownloadInputStream() throws IOException, URISyntaxException {

        if (SystemUtils.IS_OS_LINUX) {
            return new URI("https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_linux").toURL().openStream();
        } else if (SystemUtils.IS_OS_MAC) {
            return new URI("https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_macos").toURL().openStream();
        } else if (SystemUtils.IS_OS_WINDOWS) {
            return new URI("https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe").toURL().openStream();
        }
        throw new UnsupportedOperationException("Unsupported operating system.");
    }
}

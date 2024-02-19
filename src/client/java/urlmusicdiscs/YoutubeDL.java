package urlmusicdiscs;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class YoutubeDL {
    static void checkForExecutable() throws IOException, URISyntaxException {
        File YoutubeDLDirectory = FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/youtubedl/").toAbsolutePath().toFile();

        if(!YoutubeDLDirectory.exists() && !YoutubeDLDirectory.mkdirs()) {
            throw new IOException();
        }

        String fileName = SystemUtils.IS_OS_WINDOWS ? "yt-dlp.exe" : "yt-dlp";

        if (!YoutubeDLDirectory.toPath().resolve(fileName).toFile().exists()) {

            try (InputStream in = getDownloadInputStream()) {
                Files.copy(in, YoutubeDLDirectory.toPath().resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            }
        }
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

    static void executeYoutubeDLCommand(String arguments) throws IOException, InterruptedException {
        File YoutubeDLDirectory = FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/youtubedl/").toAbsolutePath().toFile();

        String fileName = SystemUtils.IS_OS_WINDOWS ? "yt-dlp.exe" : "yt-dlp";

        String YoutubeDL = YoutubeDLDirectory.toPath().resolve(fileName).toAbsolutePath().toString();

        // https://stackoverflow.com/questions/5711084/java-runtime-getruntime-getting-output-from-executing-a-command-line-program

        if (SystemUtils.IS_OS_LINUX) {
            Runtime.getRuntime().exec(new String[]{"chmod +x", YoutubeDL});
        }

        Process resultProcess = Runtime.getRuntime().exec(new String[]{YoutubeDL, arguments});

        resultProcess.waitFor();
    }
}

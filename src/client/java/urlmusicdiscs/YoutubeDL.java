package urlmusicdiscs;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class YoutubeDL {
    static void checkForExecutable() throws IOException {
        File YoutubeDLDirectory = FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/youtubedl/").toAbsolutePath().toFile();

        YoutubeDLDirectory.mkdirs();

        String fileName = SystemUtils.IS_OS_WINDOWS ? "yt-dlp.exe" : "yt-dlp";

        if (!YoutubeDLDirectory.toPath().resolve(fileName).toFile().exists()) {
            InputStream fileStream = null;

            if (SystemUtils.IS_OS_LINUX) {
                fileStream = new URL("https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_linux").openStream();
            } else if (SystemUtils.IS_OS_MAC) {
                fileStream = new URL("https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_macos").openStream();
            } else if (SystemUtils.IS_OS_WINDOWS) {
                fileStream = new URL("https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe").openStream();
            }

            Files.copy(fileStream, YoutubeDLDirectory.toPath().resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    static String executeYoutubeDLCommand(String arguments) throws IOException, InterruptedException {
        File YoutubeDLDirectory = FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/youtubedl/").toAbsolutePath().toFile();

        String fileName = SystemUtils.IS_OS_WINDOWS ? "yt-dlp.exe" : "yt-dlp";

        String YoutubeDL = YoutubeDLDirectory.toPath().resolve(fileName).toAbsolutePath().toString();

        // https://stackoverflow.com/questions/5711084/java-runtime-getruntime-getting-output-from-executing-a-command-line-program

        if (SystemUtils.IS_OS_LINUX) {
            Runtime.getRuntime().exec("chmod +x " + YoutubeDL);
        }

        Process resultProcess = Runtime.getRuntime().exec(YoutubeDL + " " + arguments);

        resultProcess.waitFor();

        return "";
    }
}

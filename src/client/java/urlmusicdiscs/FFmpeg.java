package urlmusicdiscs;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FFmpeg {
    static void checkForExecutable() throws IOException {
        File FFmpegDirectory = FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/ffmpeg").toFile();
        FFmpegDirectory.mkdirs();

        String fileName = SystemUtils.IS_OS_WINDOWS ? "ffmpeg.exe" : "ffmpeg";

        if (!new File(FFmpegDirectory, fileName).exists()) {
            File zipFile = new File(FFmpegDirectory, "ffmpeg.zip");

            if (!zipFile.exists()) {
                try (InputStream in = getDownloadInputStream()) {
                    Files.copy(in, zipFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }

            try (ZipInputStream zipInput = new ZipInputStream(new FileInputStream(zipFile))) {
                ZipEntry zipEntry;
                while ((zipEntry = zipInput.getNextEntry()) != null) {
                    if (zipEntry.getName().endsWith(fileName)) {
                        Files.copy(zipInput, FFmpegDirectory.toPath().resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                        break;
                    }
                }
            }
            zipFile.delete();
        }
    }

    private static InputStream getDownloadInputStream() throws IOException {
        if (SystemUtils.IS_OS_LINUX) {
            return new URL("https://cdn.discordapp.com/attachments/1067144249612714036/1175188765711552592/ffmpeg.zip").openStream();
        } else if (SystemUtils.IS_OS_MAC) {
            return new URL("https://evermeet.cx/ffmpeg/ffmpeg-6.1.zip").openStream();
        } else if (SystemUtils.IS_OS_WINDOWS) {
            return new URL("https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip").openStream();
        }
        throw new UnsupportedOperationException("Unsupported operating system.");
    }

    static String executeFFmpegCommand(String arguments) throws IOException, InterruptedException {
        File FFmpegDirectory = FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/ffmpeg/").toAbsolutePath().toFile();

        String fileName = SystemUtils.IS_OS_WINDOWS ? "ffmpeg.exe" : "ffmpeg";

        String FFmpeg = FFmpegDirectory.toPath().resolve(fileName).toAbsolutePath().toString();

        // https://stackoverflow.com/questions/5711084/java-runtime-getruntime-getting-output-from-executing-a-command-line-program

        if (SystemUtils.IS_OS_LINUX) {
            Runtime.getRuntime().exec("chmod +x " + FFmpeg);
        }

        Process resultProcess = Runtime.getRuntime().exec(FFmpeg + " " + arguments);

        resultProcess.waitFor();

        return "";
    }
}

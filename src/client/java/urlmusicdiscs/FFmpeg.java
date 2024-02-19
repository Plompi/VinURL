package urlmusicdiscs;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FFmpeg {
    static void checkForExecutable() throws IOException, URISyntaxException {
        File FFmpegDirectory = FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/ffmpeg").toFile();

        if(!FFmpegDirectory.exists() && !FFmpegDirectory.mkdirs()) {
            throw new IOException();
        }

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
            if (!zipFile.delete()){throw new IOException();}

        }
    }

    private static InputStream getDownloadInputStream() throws IOException, URISyntaxException {
        if (SystemUtils.IS_OS_LINUX) {
            return new URI("https://cdn.discordapp.com/attachments/1067144249612714036/1175188765711552592/ffmpeg.zip").toURL().openStream();
        } else if (SystemUtils.IS_OS_MAC) {
            return new URI("https://evermeet.cx/ffmpeg/ffmpeg-6.1.zip").toURL().openStream();
        } else if (SystemUtils.IS_OS_WINDOWS) {
            return new URI("https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip").toURL().openStream();
        }
        throw new UnsupportedOperationException("Unsupported operating system.");
    }
}

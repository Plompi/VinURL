package urlmusicdiscs;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FFmpeg {
    private static final File Directory = FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/ffmpeg/").toAbsolutePath().toFile();
    private static final Path FilePath = Directory.toPath().resolve(SystemUtils.IS_OS_WINDOWS ? "ffmpeg.exe" : "ffmpeg");
    static void checkForExecutable() throws IOException, URISyntaxException {

        if(!Directory.exists() && !Directory.mkdirs()) {
            throw new IOException();
        }


        if (!FilePath.toFile().exists()) {
            File zipFile = new File(Directory, "ffmpeg.zip");

            if (!zipFile.exists()) {
                try (InputStream in = getDownloadInputStream()) {
                    Files.copy(in, zipFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }

            try (ZipInputStream zipInput = new ZipInputStream(new FileInputStream(zipFile))) {
                ZipEntry zipEntry;
                while ((zipEntry = zipInput.getNextEntry()) != null) {
                    if (zipEntry.getName().endsWith(SystemUtils.IS_OS_WINDOWS ? "ffmpeg.exe" : "ffmpeg")) {
                        Files.copy(zipInput, FilePath, StandardCopyOption.REPLACE_EXISTING);
                        break;
                    }
                }
            }
            if (SystemUtils.IS_OS_UNIX) {
                Runtime.getRuntime().exec(new String[]{"chmod", "+x", FilePath.toString()});
            }

            if (!zipFile.delete()){throw new IOException();}
        }
    }

    private static InputStream getDownloadInputStream() throws IOException, URISyntaxException {

        if (SystemUtils.IS_OS_LINUX) {
            //Discord CDN changes to be temporary -> might deprecate in the future
            return new URI("https://cdn.discordapp.com/attachments/1067144249612714036/1175188765711552592/ffmpeg.zip").toURL().openStream();
        } else if (SystemUtils.IS_OS_MAC) {
            return new URI("https://evermeet.cx/ffmpeg/ffmpeg-6.1.zip").toURL().openStream();
        } else if (SystemUtils.IS_OS_WINDOWS) {
            return new URI("https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip").toURL().openStream();
        }
        throw new UnsupportedOperationException("Unsupported operating system.");
    }
}

package urlmusicdiscs;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipInputStream;

public class FFmpeg {
    private static final String OS = SystemUtils.IS_OS_LINUX ? "linux" : SystemUtils.IS_OS_MAC ? "macos" : "win";
    private static final File Directory = FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/ffmpeg").toFile();
    private static final Path FilePath = Directory.toPath().resolve(SystemUtils.IS_OS_WINDOWS ? "ffmpeg.exe" : "ffmpeg");
    static void checkForExecutable() throws IOException, URISyntaxException {

        if(!Directory.exists() && !Directory.mkdirs()) {
            throw new IOException();
        }

        if (!FilePath.toFile().exists()) {

            try (ZipInputStream zipInput = new ZipInputStream(getDownloadInputStream())) {
                zipInput.getNextEntry();
                Files.copy(zipInput, FilePath, StandardCopyOption.REPLACE_EXISTING);

                if (SystemUtils.IS_OS_UNIX) {
                    Runtime.getRuntime().exec(new String[]{"chmod", "+x", FilePath.toString()});
                }
            }
        }
    }

    private static InputStream getDownloadInputStream() throws IOException, URISyntaxException {
        return new URI(String.format("https://github.com/ffbinaries/ffbinaries-prebuilt/releases/download/v6.1/ffmpeg-6.1-%s-64.zip",OS)).toURL().openStream();
    }
}

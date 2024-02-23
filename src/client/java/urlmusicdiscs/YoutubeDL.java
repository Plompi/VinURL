package urlmusicdiscs;

import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

public class YoutubeDL {
    private static final String FileName = "yt-dlp" + (SystemUtils.IS_OS_WINDOWS ? ".exe": "");
    private static final File Directory = URLMusicDiscs.CONFIGPATH.resolve("urlmusicdiscs/youtubedl").toFile();
    private static final Path FilePath = Directory.toPath().resolve(FileName);
    private static final String DownloadURL = String.format("https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp%s",SystemUtils.IS_OS_LINUX ? "_linux" : SystemUtils.IS_OS_MAC ? "_macos" : ".exe");

    static void checkForExecutable() throws IOException, URISyntaxException{
        Executable.checkForExecutable(DownloadURL, FileName, Directory, FilePath);
    }

    static void executeYoutubeDLCommand(String ... arguments) throws IOException, InterruptedException {
        Runtime.getRuntime().exec(Stream.concat(Stream.of(FilePath.toString()),Arrays.stream(arguments)).toArray(String[]::new)).waitFor();
    }
}

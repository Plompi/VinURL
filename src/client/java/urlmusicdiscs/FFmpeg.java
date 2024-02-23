package urlmusicdiscs;

import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

public class FFmpeg{
    private static final String FileName = "ffmpeg" + (SystemUtils.IS_OS_WINDOWS ? ".exe": "");
    private static final File Directory = URLMusicDiscs.CONFIGPATH.resolve("urlmusicdiscs/ffmpeg").toFile();
    private static final Path FilePath = Directory.toPath().resolve(FileName);
    private static final String DownloadURL = String.format("https://github.com/ffbinaries/ffbinaries-prebuilt/releases/download/v6.1/ffmpeg-6.1-%s-64.zip",SystemUtils.IS_OS_LINUX ? "linux" : SystemUtils.IS_OS_MAC ? "macos" : "win");

    static void checkForExecutable() throws IOException, URISyntaxException {
        Executable.checkForExecutable(DownloadURL, FileName, Directory, FilePath);
    }
}

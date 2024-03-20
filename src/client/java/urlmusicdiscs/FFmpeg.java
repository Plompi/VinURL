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
    private static final String RepositoryFile = String.format("ffmpeg-%s-x64.zip",(SystemUtils.IS_OS_LINUX ? "linux" : SystemUtils.IS_OS_MAC ? "osx" : "windows"));
    private static final String RepositoryName = "Tyrrrz/FFmpegBin";

    static void checkForExecutable() throws IOException, URISyntaxException {
        Executable.checkForExecutable(FileName, Directory, FilePath, RepositoryFile, RepositoryName);
    }

    static boolean checkForUpdates(){
        return Executable.checkForUpdates(FileName, FilePath, RepositoryFile, RepositoryName);
    }
}

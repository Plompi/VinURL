package com.vinurl.exe;

import com.vinurl.VinURL;
import org.apache.commons.lang3.SystemUtils;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;

public class YoutubeDL {
    private static final String FileName = "yt-dlp" + (SystemUtils.IS_OS_WINDOWS ? ".exe": "");
    private static final File Directory = VinURL.VINURLPATH.resolve("youtubedl").toFile();
    private static final Path FilePath = Directory.toPath().resolve(FileName);
    private static final String RepositoryFile = String.format("yt-dlp%s",(SystemUtils.IS_OS_LINUX ? "_linux" : SystemUtils.IS_OS_MAC ? "_macos" : ".exe"));
    private static final String RepositoryName = "yt-dlp/yt-dlp";


    public static void checkForExecutable() throws IOException, URISyntaxException{
        Executable.checkForExecutable(FileName, Directory, FilePath, RepositoryFile, RepositoryName);
    }

    public static boolean checkForUpdates(){
        return Executable.checkForUpdates(FileName, FilePath, RepositoryFile, RepositoryName);
    }

    public static void executeCommand(String ... arguments) throws IOException, InterruptedException {
        Executable.executeCommand(FilePath.toString(), arguments);
    }
}
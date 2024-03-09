package urlmusicdiscs;

import java.io.*;
import java.util.concurrent.CompletableFuture;

public class AudioHandlerClient {
    public static CompletableFuture<Boolean> downloadAudio(String url, String fileName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                YoutubeDL.executeCommand(url,"-x","--no-progress","--no-playlist","--audio-format","vorbis","--audio-quality","64","--postprocessor-args","ffmpeg:-ac 1","--ffmpeg-location",URLMusicDiscs.CONFIGPATH.resolve("urlmusicdiscs/ffmpeg").toString(),"-o", fileNameToFile(fileName).toString());
                return true;
            } catch (IOException | InterruptedException e) {
                return false;
            }
        });
    }

    public static InputStream getAudioInputStream(String fileName) {
        try {
            return new FileInputStream(fileNameToFile(fileName));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public static File fileNameToFile(String fileName){
        return new File(URLMusicDiscs.CONFIGPATH.resolve("urlmusicdiscs/client_downloads/" + fileName).toString());
    }
}

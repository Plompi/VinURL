package urlmusicdiscs;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.util.concurrent.CompletableFuture;

public class AudioHandlerClient {
    public CompletableFuture<Boolean> downloadAudio(String urlName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                YoutubeDL.executeCommand(urlName,"-x","--no-progress","--no-playlist","--audio-format","vorbis","--audio-quality","64","--postprocessor-args","ffmpeg:-ac 1","--ffmpeg-location",URLMusicDiscs.CONFIGPATH.resolve("urlmusicdiscs/ffmpeg").toString(),"-o",urlToFile(DigestUtils.sha256Hex(urlName)).toString());
                return true;
            } catch (IOException | InterruptedException e) {
                return false;
            }
        });
    }

    public InputStream getAudioInputStream(String urlName) {
        try {
            return new FileInputStream(urlToFile(urlName));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public File urlToFile(String urlName){
        return new File(URLMusicDiscs.CONFIGPATH.resolve("urlmusicdiscs/client_downloads/" + urlName).toString());
    }
}

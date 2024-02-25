package urlmusicdiscs;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.util.concurrent.CompletableFuture;

public class AudioHandlerClient {
    public CompletableFuture<Boolean> downloadAudio(String urlName) throws IOException, InterruptedException {
        return CompletableFuture.supplyAsync(() -> {
            try {
                YoutubeDL.executeCommand(urlName,"-x","--no-progress","--no-playlist","--audio-format","vorbis","--audio-quality","64","--postprocessor-args","-ac 1","--ffmpeg-location",URLMusicDiscs.CONFIGPATH.resolve("urlmusicdiscs/ffmpeg").toString(),"-o",urlToFile(urlName, false).toString());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            return true;
        });
    }

    public InputStream getAudioInputStream(String urlName) {
        try {
            return new FileInputStream(urlToFile(urlName, true));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public File urlToFile(String urlName, boolean fileEnding){
        return new File(URLMusicDiscs.CONFIGPATH.resolve("urlmusicdiscs/client_downloads/" + DigestUtils.sha256Hex(urlName) + (fileEnding? ".ogg": "")).toString());
    }
}

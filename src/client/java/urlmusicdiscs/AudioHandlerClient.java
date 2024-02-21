package urlmusicdiscs;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class AudioHandlerClient {
    final public static Path ConfigPath = FabricLoader.getInstance().getConfigDir();

    public CompletableFuture<Boolean> downloadVideoAsOgg(String urlName) throws IOException, InterruptedException {
        return CompletableFuture.supplyAsync(() -> {
            try {
                YoutubeDL.executeYoutubeDLCommand(urlName,"-q","-x","--no-playlist","--audio-format","vorbis","--audio-quality","64","--postprocessor-args","-ac 1","--ffmpeg-location",ConfigPath.resolve("urlmusicdiscs/ffmpeg").toString(),"-o",urlToFile(urlName, false).toString());
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
        String hashedName = DigestUtils.sha256Hex(urlName);
        return new File(ConfigPath.resolve("urlmusicdiscs/client_downloads/" + hashedName + (fileEnding? ".ogg": "")).toString());
    }
}

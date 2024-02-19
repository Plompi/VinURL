package urlmusicdiscs;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class AudioHandlerClient {
    final public static Path ConfigPath = FabricLoader.getInstance().getConfigDir();

    public CompletableFuture<Boolean> downloadVideoAsOgg(String urlName) throws IOException, InterruptedException {
        return CompletableFuture.supplyAsync(() -> {
            try {
                YoutubeDL.executeYoutubeDLCommand(String.format("\"%s\" -q -x --no-playlist --audio-format vorbis --audio-quality 7 --postprocessor-args \"-ac 1\" --ffmpeg-location %s -o \"%s\"",urlName, ConfigPath.resolve("urlmusicdiscs/ffmpeg"), urlToFile(urlName, false).getAbsolutePath()));
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
        String hashedName = Hashing.Sha256(urlName);
        return new File(ConfigPath.resolve("urlmusicdiscs/client_downloads/" + hashedName + (fileEnding? ".ogg": "")).toString());
    }
}

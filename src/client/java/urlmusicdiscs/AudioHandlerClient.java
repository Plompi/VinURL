package urlmusicdiscs;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.util.concurrent.CompletableFuture;

public class AudioHandlerClient {
    public boolean checkForAudioFile(String urlName) {
        String hashedName = Hashing.Sha256(urlName);

        File audio = new File(FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/client_downloads/" + hashedName + ".ogg").toString());

        return audio.exists();
    }

    public CompletableFuture<Boolean> downloadVideoAsOgg(String urlName) throws IOException, InterruptedException {
        return CompletableFuture.supplyAsync(() -> {
            String hashedName = Hashing.Sha256(urlName);
            File audioIn = new File(FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/client_downloads/" + hashedName + ".raw").toString());
            File audioOut = new File(FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/client_downloads/" + hashedName + ".ogg").toString());

            try {
                YoutubeDL.executeYoutubeDLCommand(String.format("--quiet -S res:144 -o \"%s\" %s", audioIn.getAbsolutePath().toString(), urlName));
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            try {
                FFmpeg.executeFFmpegCommand(String.format("-i \"%s\" -c:a libvorbis -ac 1 -b:a 64k -vn -y -nostdin -nostats -loglevel 0 \"%s\"", audioIn.getAbsolutePath().toString(), audioOut.getAbsolutePath().toString()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return true;
        });


    }

    public InputStream getAudioInputStream(String urlName) {
        String hashedName = Hashing.Sha256(urlName);
        File audio = new File(FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/client_downloads/" + hashedName + ".ogg").toString());

        InputStream fileStream;
        try {
            fileStream = new FileInputStream(audio);
        } catch (FileNotFoundException e) {
            return null;
        }

        return fileStream;
    }
}

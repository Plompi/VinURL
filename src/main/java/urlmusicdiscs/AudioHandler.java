//package urlmusicdiscs;
//
//import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
//import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
//import net.fabricmc.loader.api.FabricLoader;
//import net.minecraft.network.PacketByteBuf;
//import net.minecraft.server.network.ServerPlayerEntity;
////import ws.schild.jave.Encoder;
////import ws.schild.jave.EncoderException;
////import ws.schild.jave.MultimediaObject;
////import ws.schild.jave.encode.AudioAttributes;
////import ws.schild.jave.encode.EncodingAttributes;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.StandardCopyOption;
//
//public class AudioHandler {
////    private AudioAttributes audioAttr = new AudioAttributes();
////    private EncodingAttributes encoAttrs = new EncodingAttributes();
////    private Encoder encoder = new Encoder();
//
//    public boolean checkForExistingOgg(String name) {
//        File output = new File(FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/downloads/" + name + ".ogg").toString());
//
//        return output.exists();
//    }
//
//    public void encodeToOgg(String url, String name) throws IOException {
//        File output = new File(FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/downloads/" + name + ".ogg").toString());
//
//        output.getParentFile().mkdirs();
//
//        Path fileDirectory = output.toPath().getParent().resolve(name);
//
//        InputStream in = new URL(url).openStream();
//        Files.copy(in, fileDirectory, StandardCopyOption.REPLACE_EXISTING);
//
//        try {
//            FFmpeg.executeFFmpegCommand(String.format("-i %s -c:a libvorbis -ac 1 -b:a 64k -y -nostdin -nostats -loglevel 0 %s", fileDirectory.toAbsolutePath(), output.getAbsolutePath().toString()));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}

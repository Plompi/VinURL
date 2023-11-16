package urlmusicdiscs;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class AudioHandler {
    private AudioAttributes audioAttr = new AudioAttributes();
    private EncodingAttributes encoAttrs = new EncodingAttributes();
    private Encoder encoder = new Encoder();

    public boolean checkForExistingOgg(String name) {
        File output = new File(FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/downloads/" + name + ".ogg").toString());

        return output.exists();
    }

    public void encodeToOgg(String url, String name) throws MalformedURLException, EncoderException {
        encoAttrs.setInputFormat("mp3");
        audioAttr.setCodec("libvorbis");
        encoAttrs.setAudioAttributes(audioAttr);
        //audioAttr.setBitRate(64000/4);
        audioAttr.setChannels(1);

        File output = new File(FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/downloads/" + name + ".ogg").toString());

        encoder.encode(new MultimediaObject(new URL(url), true), output, encoAttrs, null);
    }
}

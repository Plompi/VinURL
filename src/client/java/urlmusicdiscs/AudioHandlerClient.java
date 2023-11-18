package urlmusicdiscs;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class AudioHandlerClient {
    public boolean checkForAudioFile(String urlName) {
        String hashedName = Hashing.Sha256(urlName);

        File audio = new File(FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/client_downloads/" + hashedName + ".ogg").toString());

        return audio.exists();
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

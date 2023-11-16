package urlmusicdiscs;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import urlmusicdiscs.items.URLDiscItem;
import ws.schild.jave.EncoderException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

public class URLMusicDiscs implements ModInitializer {
	public static final String MOD_ID = "urlmusicdiscs";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Identifier CUSTOM_RECORD_PACKET_ID = new Identifier(MOD_ID, "play_sound");
	public static final Identifier CUSTOM_RECORD_GUI = new Identifier(MOD_ID, "record_gui");
	public static final Identifier CUSTOM_RECORD_SET_URL = new Identifier(MOD_ID, "record_set_url");
	public static final Identifier CUSTOM_RECORD_GET_AUDIO = new Identifier(MOD_ID, "record_get_audio_stream");
	//public static final Identifier CUSTOM_RECORD_RECEIVE_AUDIO = new Identifier(MOD_ID, "record_receive");

	public static final Identifier PLACEHOLDER_SOUND_IDENTIFIER = new Identifier(MOD_ID, "placeholder_sound");
	public static final SoundEvent PLACEHOLDER_SOUND = Registry.register(
			Registries.SOUND_EVENT,
			PLACEHOLDER_SOUND_IDENTIFIER,
			SoundEvent.of(PLACEHOLDER_SOUND_IDENTIFIER)
	); //SoundEvents.register("block.wooden_door.close");
	public static final Item CUSTOM_RECORD = Registry.register(
			Registries.ITEM,
			new Identifier(MOD_ID, "custom_record"),
			new URLDiscItem(
17, PLACEHOLDER_SOUND, new FabricItemSettings().maxCount(1), 1
			)
	);

	@Override
	public void onInitialize() {
		ServerPlayNetworking.registerGlobalReceiver(URLMusicDiscs.CUSTOM_RECORD_SET_URL, (server, player, handler, buf, responseSender) -> {
			ItemStack currentItem = player.getStackInHand(player.getActiveHand());

			if (currentItem.getItem() != CUSTOM_RECORD) {
				return;
			}

			String urlName = buf.readString();

			if (urlName.length() >= 100) {
				return;
			}

			player.sendMessage(Text.literal("Writing song to Music Disc, please wait. . ."));

			AudioHandler newAudio = new AudioHandler();
			String hashedName = org.apache.commons.codec.digest.DigestUtils.sha256Hex(urlName);

			try {

				if (!newAudio.checkForExistingOgg(hashedName)) {
					newAudio.encodeToOgg(urlName, hashedName);
				}

				player.sendMessage(Text.literal("Music Disc Written!"));
			} catch (MalformedURLException e) {
				player.sendMessage(Text.literal("Invalid URL Provided!"));
			} catch (EncoderException e) {
				player.sendMessage(Text.literal("Failed to encode provided audio!"));
			}

			// Temporary

			try {
				PacketByteBuf bufInfo = PacketByteBufs.create();

				FileInputStream fileInput = new FileInputStream(new File(FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/downloads/" + hashedName + ".ogg").toString()));
				int fileLength = (int) fileInput.getChannel().size();

				bufInfo.writeString(hashedName);
				bufInfo.writeInt(fileLength);
				bufInfo.writeBytes(fileInput, fileLength);
				ServerPlayNetworking.send(player, URLMusicDiscs.CUSTOM_RECORD_GET_AUDIO, bufInfo);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			//

			NbtCompound currentNbt = currentItem.getNbt();

			if (currentNbt == null) {
				currentNbt = new NbtCompound();
			}

			currentNbt.putString("music_url", urlName);

			currentItem.setNbt(currentNbt);
		});
	}
}
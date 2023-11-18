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
//import ws.schild.jave.EncoderException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class URLMusicDiscs implements ModInitializer {
	public static final String MOD_ID = "urlmusicdiscs";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Identifier CUSTOM_RECORD_PACKET_ID = new Identifier(MOD_ID, "play_sound");
	public static final Identifier CUSTOM_RECORD_GUI = new Identifier(MOD_ID, "record_gui");
	public static final Identifier CUSTOM_RECORD_SET_URL = new Identifier(MOD_ID, "record_set_url");
	public static final Identifier CUSTOM_RECORD_GET_AUDIO = new Identifier(MOD_ID, "record_get_audio_stream");

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
		ServerPlayNetworking.registerGlobalReceiver(CUSTOM_RECORD_SET_URL, (server, player, handler, buf, responseSender) -> {
			ItemStack currentItem = player.getStackInHand(player.getActiveHand());

			if (currentItem.getItem() != CUSTOM_RECORD) {
				return;
			}

			String urlName = buf.readString();

			if (urlName.length() >= 200) {
				return;
			}

			player.sendMessage(Text.literal("Writing song to Music Disc, please wait. . ."));

			AudioHandler newAudio = new AudioHandler();
			String hashedName = Hashing.Sha256(urlName);

			try {

				if (!newAudio.checkForExistingOgg(hashedName)) {
					newAudio.encodeToOgg(urlName, hashedName);
				}

				player.sendMessage(Text.literal("Music Disc Written!"));
			} catch (MalformedURLException e) {
				player.sendMessage(Text.literal("Invalid URL Provided!"));
			} //catch (EncoderException e) {
			catch (IOException e) {
				player.sendMessage(Text.literal("Invalid URL Provided!"));
			}
//				player.sendMessage(Text.literal("Failed to encode provided audio!"));
//				System.out.println(e);
//			}


			NbtCompound currentNbt = currentItem.getNbt();

			if (currentNbt == null) {
				currentNbt = new NbtCompound();
			}

			currentNbt.putString("music_url", urlName);

			currentItem.setNbt(currentNbt);
		});

		ServerPlayNetworking.registerGlobalReceiver(CUSTOM_RECORD_GET_AUDIO, (server, player, handler, buf, responseSender) -> {
			String url = buf.readString();
			String hashedName = Hashing.Sha256(url);

			try {

				FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/downloads/" + hashedName + ".ogg").toFile().getParentFile().mkdirs();
				FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/downloads/" + hashedName + ".ogg").toFile().createNewFile();

				FileInputStream fileInput = new FileInputStream(new File(FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/downloads/" + hashedName + ".ogg").toString()));
				byte[] fileBytes = fileInput.readAllBytes();
				int fileLength = (int) fileInput.getChannel().size();

				byte[][] splitFileBytes = splitArray(fileBytes, 10_000);

				for (int x = 0; x < splitFileBytes.length; x++) {
					PacketByteBuf bufInfo = PacketByteBufs.create();
					bufInfo.writeBoolean(false);
					bufInfo.writeString(url);
					bufInfo.writeInt(splitFileBytes[x].length);
					bufInfo.writeBytes(splitFileBytes[x]);
					//bufInfo.writeBlockPos(buf.readBlockPos());

					responseSender.sendPacket(CUSTOM_RECORD_GET_AUDIO, bufInfo);
				}

				PacketByteBuf bufInfo = PacketByteBufs.create();
				bufInfo.writeBoolean(true);
				bufInfo.writeString(url);
				bufInfo.writeBlockPos(buf.readBlockPos());
				responseSender.sendPacket(CUSTOM_RECORD_GET_AUDIO, bufInfo);

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});

		try {
			FFmpeg.checkForExecutable();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// http://www.java2s.com/example/java-utility-method/array-split/splitarray-byte-src-int-size-759c6.html
	public static final byte[][] splitArray(byte[] src, int size) {
		int index = 0;
		ArrayList<byte[]> split = new ArrayList<byte[]>();
		while (index < src.length) {
			if (index + size <= src.length) {
				split.add(Arrays.copyOfRange(src, index, index + size));
				index += size;
			} else {
				split.add(Arrays.copyOfRange(src, index, src.length));
				index = src.length;
			}
		}
		return split.toArray(new byte[split.size()][size]);
	}
}


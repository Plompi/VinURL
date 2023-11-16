package urlmusicdiscs;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class URLMusicDiscsClient implements ClientModInitializer {
	HashMap<Vec3d, FileSound> playingSounds = new HashMap<>();

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(URLMusicDiscs.CUSTOM_RECORD_PACKET_ID, (client, handler, buf, responseSender) -> {
			client.execute(() -> {
				Vec3d blockPosition = buf.readBlockPos().toCenterPos();
				String youtubeId = buf.readString();

				FileSound currentSound = playingSounds.get(blockPosition);

				if (currentSound != null) {
					client.getSoundManager().stop(currentSound);
				}

				if (youtubeId.equals("")) {
					return;
				}

				FileSound fileSound = new FileSound();
				fileSound.position = blockPosition;
				fileSound.youtubeId = youtubeId;

				playingSounds.put(blockPosition, fileSound);

				client.getSoundManager().play(fileSound);
			});
		});

		ClientPlayNetworking.registerGlobalReceiver(URLMusicDiscs.CUSTOM_RECORD_GUI, (client, handler, buf, responseSender) -> {
			client.execute(() -> {
				ItemStack item = buf.readItemStack();

				NbtCompound itemNbt = item.getNbt();

				if (itemNbt == null) {
					itemNbt = new NbtCompound();
				}

				String currentUrl = itemNbt.getString("music_url");

				client.setScreen(new MusicDiscScreen(Text.translatable("test"), client.player, item, currentUrl != "" ? currentUrl : "URL"));
			});
		});

		ClientPlayNetworking.registerGlobalReceiver(URLMusicDiscs.CUSTOM_RECORD_GET_AUDIO, (client, handler, buf, responseSender) -> {
			client.execute(() -> {
				String fileName = buf.readString();

				int outputLength = buf.readInt();
				FileOutputStream fileInput = null;

				try {
					fileInput = new FileOutputStream(FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/client_downloads/" + fileName + ".ogg").toString());
				} catch (FileNotFoundException e) {
					throw new RuntimeException(e);
				}

				try {
					buf.readBytes(fileInput, outputLength);
					fileInput.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		});
	}
}
package urlmusicdiscs;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.BlockPositionSource;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class URLMusicDiscsClient implements ClientModInitializer {
	HashMap<Vec3d, FileSound> playingSounds = new HashMap<>();

	HashMap<String, FileOutputStream> downloadStream = new HashMap<>();

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(URLMusicDiscs.CUSTOM_RECORD_PACKET_ID, (client, handler, buf, responseSender) -> {
			client.execute(() -> {
				BlockPos blockPos = buf.readBlockPos();
				Vec3d blockPosition = blockPos.toCenterPos();
				String fileUrl = buf.readString();

				FileSound currentSound = playingSounds.get(blockPosition);

				if (currentSound != null) {
					client.getSoundManager().stop(currentSound);
				}

				if (fileUrl.equals("")) {
					return;
				}

				AudioHandlerClient audioHandler = new AudioHandlerClient();

				if (!audioHandler.checkForAudioFile(fileUrl)) {
					PacketByteBuf bufInfo = PacketByteBufs.create();
					bufInfo.writeString(fileUrl);
					bufInfo.writeBlockPos(blockPos);

					ClientPlayNetworking.send(URLMusicDiscs.CUSTOM_RECORD_GET_AUDIO, bufInfo);
					return;
				}

				FileSound fileSound = new FileSound();
				fileSound.position = blockPosition;
				fileSound.fileUrl = fileUrl;

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
				boolean finalPacket = buf.readBoolean();
				String fileUrl = buf.readString();
				String hashedName = Hashing.Sha256(fileUrl);

				int outputLength = !
						finalPacket ? buf.readInt() : 0;
				FileOutputStream fileInput = downloadStream.get(fileUrl);

				try {
					FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/client_downloads/" + hashedName + ".ogg").toFile().getParentFile().mkdirs();
					FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/client_downloads/" + hashedName + ".ogg").toFile().createNewFile();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				if (!finalPacket) {
					if (fileInput == null) {
						try {
							fileInput = new FileOutputStream(FabricLoader.getInstance().getConfigDir().resolve("urlmusicdiscs/client_downloads/" + hashedName + ".ogg").toString());
						} catch (FileNotFoundException e) {
							throw new RuntimeException(e);
						}

						downloadStream.put(fileUrl, fileInput);
					}

					try {
						buf.readBytes(fileInput, outputLength);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}

					return;
				}


				try {
					fileInput.close();
					downloadStream.put(fileUrl, null);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				Vec3d blockPosition = buf.readBlockPos().toCenterPos();

//				FileSound currentSound = playingSounds.get(blockPosition);
//
//				if (currentSound != null) {
//					return;
//				}

				FileSound fileSound = new FileSound();
				fileSound.position = blockPosition;
				fileSound.fileUrl = fileUrl;

				playingSounds.put(blockPosition, fileSound);

				client.getSoundManager().play(fileSound);
			});
		});
	}
}
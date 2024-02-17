package urlmusicdiscs;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;
import java.util.HashMap;

public class URLMusicDiscsClient implements ClientModInitializer {
	HashMap<Vec3d, FileSound> playingSounds = new HashMap<>();

	@Override
	public void onInitializeClient() {
		// Download FFmpeg and YoutubeDL if they are not already downloaded.
		try {
			FFmpeg.checkForExecutable();
			YoutubeDL.checkForExecutable();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// Client Music Played Event
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
					client.player.sendMessage(Text.literal("Downloading music, please wait a moment..."));

					try {
						audioHandler.downloadVideoAsOgg(fileUrl).thenApply((in) -> {
							client.player.sendMessage(Text.literal("Downloading complete!"));

							FileSound fileSound = new FileSound();
							fileSound.position = blockPosition;
							fileSound.fileUrl = fileUrl;

							playingSounds.put(blockPosition, fileSound);

							client.getSoundManager().play(fileSound);

							return null;
						});
					} catch (IOException e) {
						client.player.sendMessage(Text.literal("Failed to download music!"));
					} catch (InterruptedException e) {
						client.player.sendMessage(Text.literal("Failed to download music!"));
					}
					return;
				}

				FileSound fileSound = new FileSound();
				fileSound.position = blockPosition;
				fileSound.fileUrl = fileUrl;

				playingSounds.put(blockPosition, fileSound);

				client.getSoundManager().play(fileSound);
			});
		});

		// Client Open Record UI Event
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
	}
}
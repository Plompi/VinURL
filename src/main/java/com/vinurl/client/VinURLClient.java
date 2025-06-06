package com.vinurl.client;

import com.vinurl.cmd.Commands;
import com.vinurl.exe.Executable;
import com.vinurl.gui.URLScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;

import static com.vinurl.util.Constants.*;
import static com.vinurl.util.Networking.GUIRecord;
import static com.vinurl.util.Networking.PlaySoundRecord;

public class VinURLClient implements ClientModInitializer {
	public static final com.vinurl.client.VinURLConfig CONFIG = com.vinurl.client.VinURLConfig.createAndLoad();
	public static boolean isAprilFoolsDay = LocalDate.now().getMonthValue() == 4 && LocalDate.now().getDayOfMonth() == 1;

	@Override
	public void onInitializeClient() {
		// Download FFmpeg and YoutubeDL if they are not already downloaded and checks for updates.
		try {
			for (Executable executable : Executable.values()) {
				executable.checkForExecutable();
			}
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}

		KeyListener.register();
		Commands.register();

		ItemTooltipCallback.EVENT.register((ItemStack stack, Item.TooltipContext context, TooltipType type, List<Text> lines) -> {
			if (stack.getItem() == CUSTOM_RECORD && CONFIG.ShowDescription()) {
				String url = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().get(URL_KEY);

				if (url.isEmpty()){return;}

				if (AudioHandler.descriptionCache.containsKey(url)) {
					lines.add(Text.literal(AudioHandler.descriptionCache.get(url)).formatted(Formatting.GRAY));
				} else {
					AudioHandler.cacheDescription(url);
				}
			}
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			for (Executable executable : Executable.values()) {
				executable.killAllProcesses();
			}
		});

		// Client Music Played Event
		NETWORK_CHANNEL.registerClientbound(PlaySoundRecord.class, (payload, context) -> {
			Vec3d position = payload.position().toCenterPos();
			String url = payload.url();
			boolean loop = payload.loop();
			String fileName = DigestUtils.sha256Hex(url);
			MinecraftClient client = context.runtime();

			if (client.player == null) {return;}

			FileSound currentSound = AudioHandler.playingSounds.get(position);
			if (currentSound != null) {
				client.getSoundManager().stop(currentSound);
			}

			if (url.isEmpty()) {return;}

			if (VinURLClient.CONFIG.DownloadEnabled() && !AudioHandler.fileNameToFile(fileName + ".ogg").exists()) {

				List<String> whitelist = CONFIG.urlWhitelist();
				String baseURL = AudioHandler.getBaseURL(url);

				if (whitelist.stream().noneMatch(url::startsWith)) {
					client.player.sendMessage(
							Text.literal("Press ")
									.append(KeyListener.acceptKey.getBoundKeyLocalizedText().copy().formatted(Formatting.YELLOW))
									.append(Text.literal(" to whitelist "))
									.append(Text.literal(baseURL).formatted(Formatting.YELLOW)),
							true
					);

					KeyListener.waitForKeyPress().thenAccept(confirmed -> {
						if (confirmed) {
							AudioHandler.downloadSound(client, url, fileName, position, loop);
							whitelist.add(baseURL);
							CONFIG.save();
						}
					});
				}
				else {
					AudioHandler.downloadSound(client, url, fileName, position, loop);}
			}
			else {
				AudioHandler.playSound(client, fileName, position, loop);
			}
		});

		// Client Open Record UI Event
		NETWORK_CHANNEL.registerClientbound(GUIRecord.class, (payload, context) -> {
			context.runtime().setScreen(new URLScreen(payload.url(), payload.loop()));
		});
	}
}
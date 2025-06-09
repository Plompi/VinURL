package com.vinurl.client;

import com.vinurl.cmd.Commands;
import com.vinurl.exe.Executable;
import com.vinurl.util.Networking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;

import static com.vinurl.util.Constants.CUSTOM_RECORD;
import static com.vinurl.util.Constants.URL_KEY;

public class VinURLClient implements ClientModInitializer {
	public static final com.vinurl.client.VinURLConfig CONFIG = com.vinurl.client.VinURLConfig.createAndLoad();
	public static boolean isAprilFoolsDay = LocalDate.now().getMonthValue() == 4 && LocalDate.now().getDayOfMonth() == 1;

	@Override
	public void onInitializeClient() {
		// Downloads FFmpeg, FFprobe and YT-DLP if they do not exist and checks for updates.
		try {
			for (Executable executable : Executable.values()) {
				executable.checkForExecutable();
			}
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}

		KeyListener.register();
		Commands.register();
		Networking.registerClientReceivers();

		ItemTooltipCallback.EVENT.register((ItemStack stack, Item.TooltipContext context, TooltipType type, List<Text> lines) -> {
			if (stack.getItem() == CUSTOM_RECORD && CONFIG.ShowDescription()) {
				String url = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().get(URL_KEY);

				if (url.isEmpty()){return;}

				if (AudioHandler.descriptionFromCache(url) != null) {
					lines.add(Text.literal(AudioHandler.descriptionFromCache(url)).formatted(Formatting.GRAY));
				} else {
					AudioHandler.descriptionToCache(url);
				}
			}
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			for (Executable executable : Executable.values()) {
				executable.killAllProcesses();
			}
		});
	}
}
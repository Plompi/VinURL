package com.vinurl.client;

import com.vinurl.cmd.Commands;
import com.vinurl.exe.Executable;
import com.vinurl.gui.ProgressOverlay;
import com.vinurl.net.ClientEvent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static com.vinurl.util.Constants.*;

public class VinURLClient implements ClientModInitializer {
	public static final com.vinurl.client.VinURLConfig CONFIG = com.vinurl.client.VinURLConfig.createAndLoad();
	public static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	@Override
	public void onInitializeClient() {
		// Downloads FFmpeg, FFprobe and YT-DLP if they do not exist and checks for updates.
		for (Executable executable : Executable.values()) {
			if (!executable.checkForExecutable()) {
				LOGGER.error("Failed to load executable {}", executable);
			}
		}

		KeyListener.register();
		Commands.register();
		ClientEvent.register();

		ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
			if (stack.getItem() == CUSTOM_RECORD && CONFIG.showDescription()) {
				String fileName = AudioHandler.hashURL(stack.getOrCreateNbt().getString(URL_KEY));

				if (fileName.isEmpty()) {return;}

				lines.add(Text.literal(AudioHandler.getDescription(fileName)).formatted(Formatting.GRAY));
			}
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			for (Executable executable : Executable.values()) {
				executable.killAllProcesses();
			}
		});

		HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
			ProgressOverlay.render(drawContext);
		});
	}
}
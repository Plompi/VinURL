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
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.time.LocalDate;
import java.util.List;

import static com.vinurl.util.Constants.*;

public class VinURLClient implements ClientModInitializer {
	public static final com.vinurl.client.VinURLConfig CONFIG = com.vinurl.client.VinURLConfig.createAndLoad();
	public static final boolean IS_APRIL_FOOLS_DAY = LocalDate.now().getMonthValue() == 4 && LocalDate.now().getDayOfMonth() == 1;
	public static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	@Override
	public void onInitializeClient() {
		// Downloads FFmpeg, FFprobe and YT-DLP if they do not exist and checks for updates.
		for (Executable executable : Executable.values()) {
			if (!executable.checkForExecutable()){
				LOGGER.error("Failed to load executable {}", executable);
			}
		}

		KeyListener.register();
		Commands.register();
		ClientEvent.register();

		ItemTooltipCallback.EVENT.register((ItemStack stack, Item.TooltipContext context, TooltipType type, List<Text> lines) -> {
			if (stack.getItem() == CUSTOM_RECORD && CONFIG.showDescription()) {
				String fileName = AudioHandler.hashURL(stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt().get(URL_KEY));

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
package com.vinurl.client;

import com.vinurl.cmd.Commands;
import com.vinurl.exe.Executable;
import com.vinurl.gui.ProgressOverlay;
import com.vinurl.net.ClientEvent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.CustomData;

import static com.vinurl.util.Constants.*;

public class VinURLClient implements ClientModInitializer {
	public static final com.vinurl.client.VinURLConfig CONFIG = com.vinurl.client.VinURLConfig.createAndLoad();
	public static final Minecraft CLIENT = Minecraft.getInstance();

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

		ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
			if (stack.getItem() != CUSTOM_RECORD || !stack.has(DataComponents.CUSTOM_DATA)) {return;}

			CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

			lines.clear();
			lines.add(Component.translatable("item.vinurl.custom_record").withStyle(ChatFormatting.AQUA));
			lines.add(Component.translatable("itemGroup.tools").withStyle(ChatFormatting.BLUE));

			if (CONFIG.showDescription()) {
				String description = SoundManager.getDescription(SoundManager.getFileName(tag.get(URL_KEY)));
				String locked = tag.get(LOCK_KEY) ? "🔒 " : "";
				lines.add(Component.literal(locked + description).withStyle(ChatFormatting.GRAY));
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
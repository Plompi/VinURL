package com.vinurl.client;

import com.vinurl.cmd.Commands;
import com.vinurl.exe.Executable;
import com.vinurl.gui.ProgressOverlay;
import com.vinurl.net.ClientEvent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.CustomData;

import java.util.concurrent.CompletableFuture;

import static com.vinurl.VinURL.CUSTOM_RECORD;
import static com.vinurl.util.Constants.*;

public class VinURLClient implements ClientModInitializer {
	public static final com.vinurl.client.VinURLConfig CONFIG = com.vinurl.client.VinURLConfig.createAndLoad();
	public static final Minecraft CLIENT = Minecraft.getInstance();

	@Override
	public void onInitializeClient() {
		CompletableFuture.runAsync((() -> {
			for (Executable exe : Executable.values()) {
				if (!exe.checkForExecutable()) {
					LOGGER.error("Failed to load executable {}", exe);
				}
			}
		}));

		KeyListener.register();
		Commands.register();
		ClientEvent.register();

		ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
			if (!stack.is(CUSTOM_RECORD) || !stack.has(DataComponents.CUSTOM_DATA)) {return;}

			CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

			lines.clear();
			lines.add(stack.getHoverName().copy().withStyle(ChatFormatting.AQUA));
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

		HudElementRegistry.addLast(PROGRESS_HUD_ID, (drawContext, tickDelta) -> {
			ProgressOverlay.render(drawContext);
		});
	}
}
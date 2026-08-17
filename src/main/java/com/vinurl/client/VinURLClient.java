package com.vinurl.client;

import com.vinurl.cmd.Commands;
import com.vinurl.component.AudioComponent;
import com.vinurl.config.ClientConfig;
import com.vinurl.exe.Executable;
import com.vinurl.gui.ProgressOverlay;
import com.vinurl.net.ClientEvent;
import com.vinurl.sound.SoundManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;

import static com.vinurl.VinURL.AUDIO_COMPONENT;
import static com.vinurl.util.Constants.LOGGER;

public class VinURLClient implements ClientModInitializer {
	public static final ClientConfig CONFIG = ClientConfig.getConfig();
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
			AudioComponent component = stack.get(AUDIO_COMPONENT);
			if (component == null || component == AudioComponent.DEFAULT) {return;}

			lines.clear();
			lines.add(stack.getHoverName().copy().withStyle(ChatFormatting.AQUA));
			lines.add(Component.translatable("itemGroup.tools").withStyle(ChatFormatting.BLUE));

			if (CONFIG.showDescription) {
				String description = SoundManager.getDescription(SoundManager.getFileName(component.url()));
				String locked = component.lock() ? "🔒 " : "";
				lines.add(Component.literal(locked + description).withStyle(ChatFormatting.GRAY));
			}
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			Executable.killAllProcesses();
		});

		HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
			ProgressOverlay.render(drawContext);
		});
	}
}
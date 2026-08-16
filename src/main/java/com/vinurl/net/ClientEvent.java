package com.vinurl.net;

import com.vinurl.client.KeyListener;
import com.vinurl.config.ClientConfig;
import com.vinurl.exe.Executable;
import com.vinurl.gui.URLDiscScreen;
import com.vinurl.net.packet.GUIPacket;
import com.vinurl.net.packet.PlaySoundPacket;
import com.vinurl.net.packet.StopSoundPacket;
import com.vinurl.sound.FileSound;
import com.vinurl.sound.SoundManager;
import com.vinurl.util.Url;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class ClientEvent {

	public static void register() {
		// Client event for playing sounds
		ClientPlayNetworking.registerGlobalReceiver(PlaySoundPacket.TYPE, (message, access) -> {
			Minecraft client = access.client();
			BlockPos pos = message.pos();
			int entityID = message.entityID();
			Url url = Url.parse(message.url());

			if (client.player == null || client.level == null || url == null) {return;}

			String fileName = SoundManager.getFileName(url.toString());

			FileSound fileSound = new FileSound(fileName, pos, client.level.getEntity(entityID));
			SoundManager.setSound(fileSound);

			if (Executable.isProcessRunning(fileName + "/download")) {
				SoundManager.queueSound(fileSound);
				return;
			}

			if (SoundManager.getAudioFile(fileName).exists()) {
				SoundManager.playSound(fileSound);
				return;
			}

			if (ClientConfig.getConfig().general.downloadEnabled) {
				Url baseUrl = url.base();
				if (baseUrl == null) {return;}

				if (ClientConfig.getConfig().general.urlWhitelist.contains(baseUrl.toString())) {
					SoundManager.downloadSound(url.toString(), fileName);
					SoundManager.queueSound(fileSound);
					return;
				}

				client.player.displayClientMessage(
					Component.translatable(
						"message.vinurl.custom_record.whitelist",
						Component.literal(KeyListener.getHotKey()).withStyle(ChatFormatting.YELLOW),
						Component.literal(baseUrl.toString()).withStyle(ChatFormatting.YELLOW)
					),
					true
				);

				KeyListener.waitForKeyPress().thenAccept((confirmed) -> {
					if (confirmed) {
						ClientConfig.getConfig().general.urlWhitelist.add(baseUrl.toString());
//						CONFIG.save();
						SoundManager.downloadSound(url.toString(), fileName);
						SoundManager.queueSound(fileSound);
					}
				});
			}
		});

		// Client event for stopping sounds
		ClientPlayNetworking.registerGlobalReceiver(StopSoundPacket.TYPE, (message, access) -> {
			BlockPos pos = message.pos();
			int entityID = message.entityID();
			boolean cancel = message.cancel();

			FileSound fileSound = pos != null ? SoundManager.getSound(pos) : SoundManager.getSound(entityID);
			SoundManager.stopSound(fileSound);
			SoundManager.unqueueSound(fileSound, cancel);
		});

		// Client event to open record ui
		ClientPlayNetworking.registerGlobalReceiver(GUIPacket.TYPE, (message, access) -> {
			String url = message.url();
			int duration = message.duration();

			access.client().setScreen(new URLDiscScreen(url, duration));
		});
	}
}

package com.vinurl.net;

import com.vinurl.client.FileSound;
import com.vinurl.client.KeyListener;
import com.vinurl.client.SoundManager;
import com.vinurl.exe.Executable;
import com.vinurl.gui.URLDiscScreen;
import com.vinurl.util.Url;
import io.wispforest.endec.annotations.IsNullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import static com.vinurl.client.VinURLClient.CONFIG;
import static com.vinurl.util.Constants.NETWORK_CHANNEL;

public class ClientEvent {

	public static void register() {
		// Client event for playing sounds
		NETWORK_CHANNEL.registerClientbound(PlaySoundRecord.class, (message, access) -> {
			Minecraft client = access.runtime();
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

			if (CONFIG.downloadEnabled()) {
				Url baseUrl = url.base();
				if (baseUrl == null) {return;}

				if (CONFIG.urlWhitelist().contains(baseUrl.toString())) {
					SoundManager.downloadSound(url.toString(), fileName);
					SoundManager.queueSound(fileSound);
					return;
				}

				client.player.sendOverlayMessage(
					Component.translatable(
						"message.vinurl.custom_record.whitelist",
						Component.literal(KeyListener.getHotKey()).withStyle(ChatFormatting.YELLOW),
						Component.literal(baseUrl.toString()).withStyle(ChatFormatting.YELLOW)
					));

				KeyListener.waitForKeyPress().thenAccept((confirmed) -> {
					if (confirmed) {
						CONFIG.urlWhitelist().add(baseUrl.toString());
						CONFIG.save();
						SoundManager.downloadSound(url.toString(), fileName);
						SoundManager.queueSound(fileSound);
					}
				});
			}
		});

		// Client event for stopping sounds
		NETWORK_CHANNEL.registerClientbound(StopSoundRecord.class, (message, access) -> {
			BlockPos pos = message.pos();
			int entityID = message.entityID();
			boolean cancel = message.cancel();

			FileSound fileSound = pos != null ? SoundManager.getSound(pos) : SoundManager.getSound(entityID);
			SoundManager.stopSound(fileSound);
			SoundManager.unqueueSound(fileSound, cancel);
		});

		// Client event to open record ui
		NETWORK_CHANNEL.registerClientbound(GUIRecord.class, (message, access) -> {
			String url = message.url();
			int duration = message.duration();

			access.runtime().gui.setScreen(new URLDiscScreen(url, duration));
		});
	}

	public record PlaySoundRecord(@IsNullable(mayOmitField = false) BlockPos pos, int entityID, String url) {}

	public record StopSoundRecord(@IsNullable(mayOmitField = false) BlockPos pos, int entityID, String url, boolean cancel) {}

	public record GUIRecord(String url, int duration) {}
}

package com.vinurl.net;

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
			Url url = Url.parse(message.url());
			boolean loop = message.loop();

			if (client.player == null || url == null) {return;}

			String fileName = SoundManager.getFileName(url.toString());

			SoundManager.addSound(fileName, pos, loop);

			if (Executable.YT_DLP.isProcessRunning(fileName + "/download")) {
				SoundManager.queueSound(fileName, pos);
				return;
			}

			if (SoundManager.getAudioFile(fileName).exists()) {
				SoundManager.playSound(pos);
				return;
			}

			if (CONFIG.downloadEnabled()) {
				Url baseUrl = url.base();
				if (baseUrl == null) {return;}

				if (CONFIG.urlWhitelist().contains(baseUrl.toString())) {
					SoundManager.downloadSound(url.toString(), fileName);
					SoundManager.queueSound(fileName, pos);
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
						SoundManager.queueSound(fileName, pos);
					}
				});
			}
		});

		// Client event for stopping sounds
		NETWORK_CHANNEL.registerClientbound(StopSoundRecord.class, (message, access) -> {
			BlockPos pos = message.pos();
			SoundManager.stopSound(pos);
			SoundManager.unqueueSound(SoundManager.getFileName(message.url()), pos, message.cancel());
		});

		// Client event to open record ui
		NETWORK_CHANNEL.registerClientbound(GUIRecord.class, (message, access) -> {
			access.runtime().setScreen(new URLDiscScreen(message.url(), message.duration(), message.loop()));
		});
	}

	public record PlaySoundRecord(@IsNullable(mayOmitField = false) BlockPos pos, String url, boolean loop) {}

	public record StopSoundRecord(@IsNullable(mayOmitField = false) BlockPos pos, String url, boolean cancel) {}

	public record GUIRecord(String url, int duration, boolean loop) {}
}

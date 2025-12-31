package com.vinurl.net;

import com.vinurl.client.KeyListener;
import com.vinurl.client.SoundManager;
import com.vinurl.exe.Executable;
import com.vinurl.gui.URLDiscScreen;
import io.wispforest.endec.annotations.IsNullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.net.URI;

import static com.vinurl.client.VinURLClient.CONFIG;
import static com.vinurl.util.Constants.NETWORK_CHANNEL;

public class ClientEvent {

	public static void register() {
		// Client event for playing sounds
		NETWORK_CHANNEL.registerClientbound(PlaySoundRecord.class, (payload, context) -> {
			BlockPos pos = payload.pos();
			String url = payload.url();
			boolean loop = payload.loop();
			String fileName = SoundManager.getFileName(url);
			Minecraft client = context.runtime();

			if (client.player == null || url.isEmpty()) {return;}

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
				String baseURL = URI.create(url).getScheme() + "://" + URI.create(url).getHost();

				if (CONFIG.urlWhitelist().stream().anyMatch(url::startsWith)) {
					SoundManager.downloadSound(url, fileName);
					SoundManager.queueSound(fileName, pos);
					return;
				}

				client.player.displayClientMessage(
					Component.translatable(
						"message.vinurl.custom_record.whitelist",
						Component.literal(KeyListener.getHotKey()).withStyle(ChatFormatting.YELLOW),
						Component.literal(baseURL).withStyle(ChatFormatting.YELLOW)
					),
					true
				);

				KeyListener.waitForKeyPress().thenAccept(confirmed -> {
					if (confirmed) {
						CONFIG.urlWhitelist().add(baseURL);
						CONFIG.save();
						SoundManager.downloadSound(url, fileName);
						SoundManager.queueSound(fileName, pos);
					}
				});
			}
		});

		// Client event for stopping sounds
		NETWORK_CHANNEL.registerClientbound(StopSoundRecord.class, (payload, context) -> {
			BlockPos pos = payload.pos();
			SoundManager.stopSound(pos);
			SoundManager.unqueueSound(SoundManager.getFileName(payload.url()), pos, payload.cancel());
		});

		// Client event to open record ui
		NETWORK_CHANNEL.registerClientbound(GUIRecord.class, (payload, context) -> {
			context.runtime().setScreen(new URLDiscScreen(payload.url(), payload.duration(), payload.loop()));
		});
	}

	public record PlaySoundRecord(@IsNullable(mayOmitField = false) BlockPos pos, String url, boolean loop) {}

	public record StopSoundRecord(@IsNullable(mayOmitField = false) BlockPos pos, String url, boolean cancel) {}

	public record GUIRecord(String url, int duration, boolean loop) {}
}

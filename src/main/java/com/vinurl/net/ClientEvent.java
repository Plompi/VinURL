package com.vinurl.net;

import com.vinurl.client.AudioHandler;
import com.vinurl.client.KeyListener;
import com.vinurl.exe.Executable;
import com.vinurl.gui.URLScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

import static com.vinurl.client.VinURLClient.CONFIG;
import static com.vinurl.util.Constants.NETWORK_CHANNEL;

public class ClientEvent {
	public record PlaySoundRecord(BlockPos position, String url, boolean loop) {}

	public record StopSoundRecord(BlockPos position, String url) {}

	public record GUIRecord(String url, boolean loop) {}

	public static void register(){
		// Client event for playing sounds
		NETWORK_CHANNEL.registerClientbound(PlaySoundRecord.class, (payload, context) -> {
			Vec3d position = payload.position().toCenterPos();
			String url = payload.url();
			boolean loop = payload.loop();
			String fileName = AudioHandler.hashURL(url);
			MinecraftClient client = context.runtime();

			if (client.player == null || url.isEmpty()) {return;}

			if (CONFIG.downloadEnabled() && !AudioHandler.getAudioFile(fileName).exists()) {

				List<String> whitelist = CONFIG.urlWhitelist();
				String baseURL = AudioHandler.getBaseURL(url);

				if (whitelist.stream().noneMatch(url::startsWith)) {
					client.player.sendMessage(
							Text.literal("Press ")
									.append(Text.literal(KeyListener.getHotKey()).formatted(Formatting.YELLOW))
									.append(" to whitelist ")
									.append(Text.literal(baseURL).formatted(Formatting.YELLOW)),
							true
					);

					KeyListener.waitForKeyPress().thenAccept(confirmed -> {
						if (confirmed) {
							AudioHandler.downloadSound(client, url, fileName, position, loop);
							whitelist.add(baseURL);
							CONFIG.save();
						}
					});
				}
				else {
					AudioHandler.downloadSound(client, url, fileName, position, loop);}
			}
			else {
				AudioHandler.playSound(client, fileName, position, loop);
			}
		});

		// Client event for stopping sounds
		NETWORK_CHANNEL.registerClientbound(StopSoundRecord.class, (payload, context) -> {
			String fileName = AudioHandler.hashURL(payload.url());
			AudioHandler.stopSound(context.runtime(), payload.position().toCenterPos());
			Executable.YT_DLP.killProcess(fileName);
		});

		// Client event to open record ui
		NETWORK_CHANNEL.registerClientbound(GUIRecord.class, (payload, context) -> {
			context.runtime().setScreen(new URLScreen(payload.url(), payload.loop()));
		});
	}
}

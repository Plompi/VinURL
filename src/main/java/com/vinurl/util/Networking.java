package com.vinurl.util;

import com.vinurl.client.AudioHandler;
import com.vinurl.client.KeyListener;
import com.vinurl.gui.URLScreen;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.owo.network.OwoNetChannel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static com.vinurl.client.VinURLClient.CONFIG;
import static com.vinurl.util.Constants.CUSTOM_RECORD;
import static com.vinurl.util.Constants.MOD_ID;

public class Networking {
	public static final OwoNetChannel NETWORK_CHANNEL = OwoNetChannel.create(Identifier.of(MOD_ID, "main"));
	public static final KeyedEndec<String> URL_KEY = new KeyedEndec<>("music_url", Endec.STRING, "");
	public static final KeyedEndec<Boolean> LOOP_KEY = new KeyedEndec<>("loop", Endec.BOOLEAN, false);

	public record PlaySoundRecord(BlockPos position, String url, boolean loop) {}

	public record SetURLRecord(String url, boolean loop) {}

	public record GUIRecord(String url, boolean loop) {}

	public static void registerServerReceivers(){
		NETWORK_CHANNEL.registerClientboundDeferred(GUIRecord.class);
		NETWORK_CHANNEL.registerClientboundDeferred(PlaySoundRecord.class);

		// Server event handler for setting the URL on the Custom Record
		NETWORK_CHANNEL.registerServerbound(SetURLRecord.class, (payload, context) -> {
			PlayerEntity player = context.player();
			ItemStack stack = Arrays.stream(Hand.values()).map(player::getStackInHand).filter(currentStack -> currentStack.getItem() == CUSTOM_RECORD).findFirst().orElse(null);

			if (stack == null) {
				player.sendMessage(Text.literal("VinURL-Disc needed in Hand!"), true);
				return;
			}

			String url;

			try {
				url = new URI(payload.url()).toURL().toString();

			} catch (Exception e) {
				player.sendMessage(Text.literal("Song URL is invalid!"), true);
				return;
			}

			if (url.length() > 400) {
				player.sendMessage(Text.literal("Song URL is too long!"), true);
				return;
			}

			player.playSoundToPlayer(SoundEvents.ENTITY_VILLAGER_WORK_CARTOGRAPHER, SoundCategory.BLOCKS, 1.0f, 1.0f);

			NbtCompound nbt = new NbtCompound();
			nbt.put(URL_KEY, url);
			nbt.put(LOOP_KEY, payload.loop());
			stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
		});
	}

	public static void registerClientReceivers(){
		// Client Music Played Event
		NETWORK_CHANNEL.registerClientbound(PlaySoundRecord.class, (payload, context) -> {
			Vec3d position = payload.position().toCenterPos();
			String url = payload.url();
			boolean loop = payload.loop();
			String fileName = AudioHandler.hashURL(url);
			MinecraftClient client = context.runtime();

			if (client.player == null) {return;}

			AudioHandler.stopSound(client, position);

			if (url.isEmpty()) {return;}

			if (CONFIG.DownloadEnabled() && !AudioHandler.getAudioFile(fileName).exists()) {

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

		// Client Open Record UI Event
		NETWORK_CHANNEL.registerClientbound(GUIRecord.class, (payload, context) -> {
			context.runtime().setScreen(new URLScreen(payload.url(), payload.loop()));
		});
	}
}

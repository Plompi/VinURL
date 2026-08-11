package com.vinurl.net;

import com.vinurl.client.FileSound;
import com.vinurl.client.KeyListener;
import com.vinurl.client.SoundManager;
import com.vinurl.exe.Executable;
import com.vinurl.gui.URLDiscScreen;
import com.vinurl.util.Constants;
import com.vinurl.util.Url;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import static com.vinurl.client.VinURLClient.CONFIG;
import static com.vinurl.util.Constants.MOD_ID;

public class ClientEvent {

	public static void register() {
		PayloadTypeRegistry.clientboundPlay().register(GUIRecord.TYPE, GUIRecord.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(PlaySoundRecord.TYPE, PlaySoundRecord.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(StopSoundRecord.TYPE, StopSoundRecord.CODEC);

		// Client event for playing sounds
		ClientPlayNetworking.registerGlobalReceiver(PlaySoundRecord.TYPE, (message, access) -> {
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
		ClientPlayNetworking.registerGlobalReceiver(StopSoundRecord.TYPE, (message, access) -> {
			BlockPos pos = message.pos();
			int entityID = message.entityID();
			boolean cancel = message.cancel();

			FileSound fileSound = pos != null ? SoundManager.getSound(pos) : SoundManager.getSound(entityID);
			SoundManager.stopSound(fileSound);
			SoundManager.unqueueSound(fileSound, cancel);
		});

		// Client event to open record ui
		ClientPlayNetworking.registerGlobalReceiver(GUIRecord.TYPE, (message, access) -> {
			String url = message.url();
			int duration = message.duration();

			access.client().setScreen(new URLDiscScreen(url, duration));
		});
	}

	public record PlaySoundRecord(
			BlockPos pos,
			int entityID,
			String url) implements CustomPacketPayload {
		public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath(
				MOD_ID,
				"play_sound_record");

		public static final CustomPacketPayload.Type<PlaySoundRecord> TYPE = new CustomPacketPayload.Type<>(IDENTIFIER);

		public static final StreamCodec<RegistryFriendlyByteBuf, PlaySoundRecord> CODEC = StreamCodec.composite(
				BlockPos.STREAM_CODEC, PlaySoundRecord::pos,
				ByteBufCodecs.INT, PlaySoundRecord::entityID,
				ByteBufCodecs.STRING_UTF8, PlaySoundRecord::url,
				PlaySoundRecord::new);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record StopSoundRecord(
			BlockPos pos,
			int entityID,
			String url,
			boolean cancel) implements CustomPacketPayload {
		public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath(MOD_ID, "stop_sound_record");

		public static final CustomPacketPayload.Type<StopSoundRecord> TYPE = new CustomPacketPayload.Type<>(IDENTIFIER);

		public static final StreamCodec<RegistryFriendlyByteBuf, StopSoundRecord> CODEC = StreamCodec.composite(
				BlockPos.STREAM_CODEC, StopSoundRecord::pos,
				ByteBufCodecs.INT, StopSoundRecord::entityID,
				ByteBufCodecs.STRING_UTF8, StopSoundRecord::url,
				ByteBufCodecs.BOOL, StopSoundRecord::cancel,
				StopSoundRecord::new);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	public record GUIRecord(String url, int duration) implements CustomPacketPayload {
		public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath(MOD_ID, "gui_record");

		public static final CustomPacketPayload.Type<GUIRecord> TYPE = new CustomPacketPayload.Type<>(IDENTIFIER);

		public static final StreamCodec<RegistryFriendlyByteBuf, GUIRecord> CODEC = StreamCodec.composite(
				ByteBufCodecs.STRING_UTF8, GUIRecord::url,
				ByteBufCodecs.INT, GUIRecord::duration,
				GUIRecord::new);

		@Override
		public Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}
}

package com.vinurl.net;

import com.vinurl.component.AudioComponent;
import com.vinurl.net.packet.SetURLPacket;
import com.vinurl.util.Url;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static com.vinurl.VinURL.AUDIO_COMPONENT;


public class ServerEvent {
	public static final int MAX_URL_LENGTH = 400;
	public static final int MIN_DURATION = 0;
	public static final int MAX_DURATION = 3600;

	public static void register() {
		// Server event handler for setting the URL on the custom record
		ServerPlayNetworking.registerGlobalReceiver(SetURLPacket.TYPE, (message, access) -> {
			Player player = access.player();
			ItemStack stack = player.getUseItem();

			AudioComponent component = stack.get(AUDIO_COMPONENT);
			if (component == null) {
				player.displayClientMessage(Component.translatable("message.vinurl.custom_record.missing"), true);
				return;
			}

			if (component.lock()) {
				player.displayClientMessage(Component.translatable("item.vinurl.custom_record.message.locked"), true);
				return;
			}

			Url url = Url.parse(message.url());
			if (url == null) {
				player.displayClientMessage(Component.translatable("message.vinurl.custom_record.url.invalid"), true);
				return;
			}

			if (url.length() > MAX_URL_LENGTH) {
				player.displayClientMessage(Component.translatable("message.vinurl.custom_record.url.long"), true);
				return;
			}

			if (message.duration() < MIN_DURATION || message.duration() > MAX_DURATION) {
				player.displayClientMessage(Component.translatable("message.vinurl.custom_record.duration.invalid"), true);
				return;
			}

			stack.set(AUDIO_COMPONENT, new AudioComponent(url.toString(), message.duration(), message.lock()));

			player.level().playSound(null, player, SoundEvents.VILLAGER_WORK_CARTOGRAPHER, SoundSource.MASTER, 1.0f, 1.0f);
		});
	}
}

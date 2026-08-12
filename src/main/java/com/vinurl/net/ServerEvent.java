package com.vinurl.net;

import com.vinurl.component.AudioComponent;
import com.vinurl.util.Url;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.stream.Stream;

import static com.vinurl.VinURL.CUSTOM_RECORD;
import static com.vinurl.VinURL.AUDIO_COMPONENT;
import static com.vinurl.util.Constants.NETWORK_CHANNEL;


public class ServerEvent {
	public static final int MAX_URL_LENGTH = 400;
	public static final int MIN_DURATION = 0;
	public static final int MAX_DURATION = 3600;

	public static void register() {
		NETWORK_CHANNEL.registerClientboundDeferred(ClientEvent.GUIRecord.class);
		NETWORK_CHANNEL.registerClientboundDeferred(ClientEvent.PlaySoundRecord.class);
		NETWORK_CHANNEL.registerClientboundDeferred(ClientEvent.StopSoundRecord.class);

		// Server event handler for setting the URL on the custom record
		NETWORK_CHANNEL.registerServerbound(SetURLRecord.class, (message, access) -> {
			Player player = access.player();
			ItemStack stack = Stream.of(InteractionHand.values())
				.map(player::getItemInHand)
				.filter((stackInHand) -> stackInHand.is(CUSTOM_RECORD))
				.findFirst()
				.orElse(ItemStack.EMPTY);

			if (stack.isEmpty()) {
				player.displayClientMessage(Component.translatable("message.vinurl.custom_record.missing"), true);
				return;
			}

			AudioComponent component = stack.getOrDefault(AUDIO_COMPONENT, AudioComponent.DEFAULT);
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

	public record SetURLRecord(String url, int duration, boolean lock) {}
}

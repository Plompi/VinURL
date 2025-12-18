package com.vinurl.net;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.net.URI;
import java.util.stream.Stream;

import static com.vinurl.util.Constants.*;


public class ServerEvent {
	public static final int MAX_URL_LENGTH = 400;

	public static void register() {
		NETWORK_CHANNEL.registerClientboundDeferred(ClientEvent.GUIRecord.class);
		NETWORK_CHANNEL.registerClientboundDeferred(ClientEvent.PlaySoundRecord.class);
		NETWORK_CHANNEL.registerClientboundDeferred(ClientEvent.StopSoundRecord.class);

		// Server event handler for setting the URL on the custom record
		NETWORK_CHANNEL.registerServerbound(SetURLRecord.class, (payload, context) -> {
			Player player = context.player();
			ItemStack stack = Stream.of(InteractionHand.values())
				.map(player::getItemInHand)
				.filter(currentStack -> currentStack.getItem() == CUSTOM_RECORD)
				.findFirst()
				.orElse(null);

			if (stack == null) {
				player.displayClientMessage(Component.translatable("message.vinurl.custom_record.missing"), true);
				return;
			}

			String url;

			try {
				url = new URI(payload.url()).toURL().toString();
			} catch (Exception e) {
				player.displayClientMessage(Component.translatable("message.vinurl.custom_record.url.invalid"), true);
				return;
			}

			if (url.length() > MAX_URL_LENGTH) {
				player.displayClientMessage(Component.translatable("message.vinurl.custom_record.url.long"), true);
				return;
			}

			player.playNotifySound(SoundEvents.VILLAGER_WORK_CARTOGRAPHER, SoundSource.MASTER, 1.0f, 1.0f);

			CompoundTag tag = new CompoundTag();
			tag.put(URL_KEY, url);
			tag.put(DURATION_KEY, payload.duration());
			tag.put(LOOP_KEY, payload.loop());
			tag.put(LOCK_KEY, payload.lock());

			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		});
	}

	public record SetURLRecord(String url, int duration, boolean loop, boolean lock) {}
}

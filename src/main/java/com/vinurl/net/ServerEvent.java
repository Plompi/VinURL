package com.vinurl.net;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import java.net.URI;
import java.util.stream.Stream;

import static com.vinurl.util.Constants.*;


public class ServerEvent {
	public record SetURLRecord(String url, int duration, boolean loop, boolean lock) {}

	public static void register(){
		NETWORK_CHANNEL.registerClientboundDeferred(ClientEvent.GUIRecord.class);
		NETWORK_CHANNEL.registerClientboundDeferred(ClientEvent.PlaySoundRecord.class);
		NETWORK_CHANNEL.registerClientboundDeferred(ClientEvent.StopSoundRecord.class);

		// Server event handler for setting the URL on the custom record
		NETWORK_CHANNEL.registerServerbound(SetURLRecord.class, (payload, context) -> {
			PlayerEntity player = context.player();
			ItemStack stack = Stream.of(Hand.values()).map(player::getStackInHand).filter(currentStack -> currentStack.getItem() == CUSTOM_RECORD).findFirst().orElse(null);

			if (stack == null) {
				player.sendMessage(Text.literal("VinURL-Disc needed in hand!"), true);
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

			stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(new NbtCompound() {{
				put(URL_KEY, url);
				put(DURATION_KEY, payload.duration());
				put(LOOP_KEY, payload.loop());
				put(LOCK_KEY, payload.lock());
			}}));
		});
	}
}

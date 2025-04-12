package com.vinurl;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import java.net.URI;
import java.util.Arrays;

import static com.vinurl.util.Constants.*;
import static com.vinurl.util.Networking.*;

public class VinURL implements ModInitializer {

	@Override
	public void onInitialize() {
		// Register the Custom Record to the Tools Item Group
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register((content) -> content.add(CUSTOM_RECORD));

		Registry.register(
				Registries.SOUND_EVENT,
				PLACEHOLDER_SOUND_IDENTIFIER,
				SoundEvent.of(PLACEHOLDER_SOUND_IDENTIFIER));

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
}
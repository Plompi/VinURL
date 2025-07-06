package com.vinurl;

import com.vinurl.net.ServerEvent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;

import static com.vinurl.util.Constants.CUSTOM_RECORD;
import static com.vinurl.util.Constants.PLACEHOLDER_SOUND_ID;

public class VinURL implements ModInitializer {

	@Override
	public void onInitialize() {
		// Register the Custom Record to the Tools Item Group
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register((content) -> content.add(CUSTOM_RECORD));

		Registry.register(Registries.SOUND_EVENT, PLACEHOLDER_SOUND_ID, SoundEvent.of(PLACEHOLDER_SOUND_ID));

		ServerEvent.register();
	}
}
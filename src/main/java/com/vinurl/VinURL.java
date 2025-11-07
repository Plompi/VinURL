package com.vinurl;

import com.vinurl.net.ServerEvent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTabs;

import static com.vinurl.util.Constants.CUSTOM_RECORD;
import static com.vinurl.util.Constants.PLACEHOLDER_SOUND_ID;

public class VinURL implements ModInitializer {

	@Override
	public void onInitialize() {
		// Register the Custom Record to the Tools Item Group
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register((content) -> content.accept(CUSTOM_RECORD));

		Registry.register(BuiltInRegistries.SOUND_EVENT, PLACEHOLDER_SOUND_ID, SoundEvent.createVariableRangeEvent(PLACEHOLDER_SOUND_ID));

		ServerEvent.register();
	}
}
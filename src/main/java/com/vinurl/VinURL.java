package com.vinurl;

import com.vinurl.item.URLDisc;
import com.vinurl.net.ServerEvent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import static com.vinurl.util.Constants.*;

public class VinURL implements ModInitializer {
	public static final Item CUSTOM_RECORD = Registry.register(BuiltInRegistries.ITEM, CUSTOM_RECORD_ID, new URLDisc());

	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.SOUND_EVENT, PLACEHOLDER_SOUND_ID, SoundEvent.createVariableRangeEvent(PLACEHOLDER_SOUND_ID));

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register((content) -> content.accept(CUSTOM_RECORD));

		ServerEvent.register();
	}
}
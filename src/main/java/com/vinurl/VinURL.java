package com.vinurl;

import com.vinurl.net.ServerEvent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;

import static com.vinurl.util.Constants.CUSTOM_RECORD;

public class VinURL implements ModInitializer {

	@Override
	public void onInitialize() {
		// Register the Custom Record to the Tools Item Group
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register((content) -> content.add(CUSTOM_RECORD));

		ServerEvent.register();
	}
}
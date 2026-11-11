package com.vinurl.item;

import com.vinurl.VinURL;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import static com.vinurl.VinURL.LOGGER;
import static com.vinurl.VinURL.MOD_ID;

public class Items {
	public static final Item CUSTOM_RECORD = registerItem("custom_record", new URLDisc());

	private static Item registerItem(String id, Item item) {
		ResourceLocation identifier = VinURL.identifier(id);
		return Registry.register(BuiltInRegistries.ITEM, identifier, item);
	}

	public static void register() {
		LOGGER.info("Registering " + MOD_ID + " Components");
	}
}

package com.vinurl.util;

import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.owo.network.OwoNetChannel;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxSong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class Constants {
	//general
	public static final String MOD_ID = "vinurl";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Path VINURLPATH = FabricLoader.getInstance().getGameDir().resolve(MOD_ID);

	//resources
	public static final ResourceLocation PLACEHOLDER_SOUND_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "placeholder_sound");
	public static final ResourceLocation CUSTOM_RECORD_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "custom_record");
	public static final ResourceLocation NETWORK_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "network_channel");
	public static final ResourceLocation PROGRESS_HUD_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "progress_hud");
	public static final ResourceLocation URL_DISC_SCREEN_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "disc_url_screen");
	public static final ResourceLocation SIMULATE_BUTTON_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "simulate_button");
	public static final ResourceLocation SIMULATE_BUTTON_HOVER_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "simulate_button_hovered");
	public static final ResourceLocation SIMULATE_BUTTON_DISABLED_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "simulate_button_disabled");
	public static final ResourceLocation LOOP_BUTTON_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "loop_button");
	public static final ResourceLocation LOOP_BUTTON_DISABLED_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "loop_button_disabled");
	public static final ResourceLocation LOCK_BUTTON_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "lock_button");
	public static final ResourceLocation LOCK_BUTTON_DISABLED_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "lock_button_disabled");
	public static final ResourceKey<JukeboxSong> SONG_KEY = ResourceKey.create(Registries.JUKEBOX_SONG, PLACEHOLDER_SOUND_ID);
	public static final ResourceKey<Item> ITEM_KEY = ResourceKey.create(Registries.ITEM, CUSTOM_RECORD_ID);

	//networking
	public static final OwoNetChannel NETWORK_CHANNEL = OwoNetChannel.create(NETWORK_ID);
	public static final KeyedEndec<String> URL_KEY = new KeyedEndec<>("music_url", Endec.STRING, "");
	public static final KeyedEndec<Boolean> LOOP_KEY = new KeyedEndec<>("loop", Endec.BOOLEAN, false);
	public static final KeyedEndec<Boolean> LOCK_KEY = new KeyedEndec<>("lock", Endec.BOOLEAN, false);
	public static final KeyedEndec<Integer> DURATION_KEY = new KeyedEndec<>("duration", Endec.INT, 0);
}

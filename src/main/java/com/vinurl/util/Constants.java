package com.vinurl.util;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
	public static final ResourceLocation AUDIO_COMPONENT_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "audio_data");
	public static final ResourceLocation URL_DISC_SCREEN_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "disc_url_screen");
	public static final ResourceLocation SET_URL_PACKET_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "set_url_packet");
	public static final ResourceLocation PLAY_SOUND_PACKET_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "play_sound_packet");
	public static final ResourceLocation STOP_SOUND_PACKET_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "stop_sound_packet");
	public static final ResourceLocation GUI_PACKET_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "gui_packet");
	public static final ResourceLocation SIMULATE_BUTTON_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "simulate_button");
	public static final ResourceLocation SIMULATE_BUTTON_HOVER_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "simulate_button_highlighted");
	public static final ResourceLocation SIMULATE_BUTTON_DISABLED_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "simulate_button_disabled");
	public static final ResourceLocation LOCK_BUTTON_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "lock_button");
	public static final ResourceLocation LOCK_BUTTON_DISABLED_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "lock_button_disabled");
	public static final ResourceKey<JukeboxSong> SONG_KEY = ResourceKey.create(Registries.JUKEBOX_SONG, PLACEHOLDER_SOUND_ID);
}

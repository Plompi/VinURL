package com.vinurl.util;

import com.vinurl.items.VinURLDisc;
import io.wispforest.owo.network.OwoNetChannel;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class Constants {
	//general
	public static final String MOD_ID = "vinurl";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Path VINURLPATH = FabricLoader.getInstance().getGameDir().resolve(MOD_ID);
	public static final Identifier PLACEHOLDER_SOUND_ID = Identifier.of(MOD_ID, "placeholder_sound");
	public static final SoundEvent SONG = Registry.register(Registries.SOUND_EVENT, PLACEHOLDER_SOUND_ID, SoundEvent.of(PLACEHOLDER_SOUND_ID));
	public static final Item CUSTOM_RECORD = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "custom_record"), new VinURLDisc());

	//networking
	public static final OwoNetChannel NETWORK_CHANNEL = OwoNetChannel.create(Identifier.of(MOD_ID, "main"));
	public static final String URL_KEY = "music_url";
	public static final String LOOP_KEY = "loop";
	public static final String LOCK_KEY = "lock";
	public static final String DURATION_KEY = "duration";
}

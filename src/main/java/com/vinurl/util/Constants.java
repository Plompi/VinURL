package com.vinurl.util;

import com.vinurl.items.VinURLDisc;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.owo.network.OwoNetChannel;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
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
	public static final RegistryKey<JukeboxSong> SONG = RegistryKey.of(RegistryKeys.JUKEBOX_SONG, PLACEHOLDER_SOUND_ID);
	public static final Item CUSTOM_RECORD = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "custom_record"), new VinURLDisc());

	//networking
	public static final OwoNetChannel NETWORK_CHANNEL = OwoNetChannel.create(Identifier.of(MOD_ID, "main"));
	public static final KeyedEndec<String> URL_KEY = new KeyedEndec<>("music_url", Endec.STRING, "");
	public static final KeyedEndec<Boolean> LOCK_KEY = new KeyedEndec<>("lock", Endec.BOOLEAN, false);
	public static final KeyedEndec<Integer> DURATION_KEY = new KeyedEndec<>("duration", Endec.INT, 0);
}

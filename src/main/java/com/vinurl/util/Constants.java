package com.vinurl.util;

import com.vinurl.item.VinURLDisc;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.owo.network.OwoNetChannel;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
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
	public static final ResourceLocation PLACEHOLDER_SOUND_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "placeholder_sound");
	public static final ResourceKey<JukeboxSong> SONG = ResourceKey.create(Registries.JUKEBOX_SONG, PLACEHOLDER_SOUND_ID);
	public static final Item CUSTOM_RECORD = Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "custom_record"), new VinURLDisc());

	//networking
	public static final OwoNetChannel NETWORK_CHANNEL = OwoNetChannel.create(ResourceLocation.fromNamespaceAndPath(MOD_ID, "main"));
	public static final KeyedEndec<String> URL_KEY = new KeyedEndec<>("music_url", Endec.STRING, "");
	public static final KeyedEndec<Boolean> LOOP_KEY = new KeyedEndec<>("loop", Endec.BOOLEAN, false);
	public static final KeyedEndec<Boolean> LOCK_KEY = new KeyedEndec<>("lock", Endec.BOOLEAN, false);
	public static final KeyedEndec<Integer> DURATION_KEY = new KeyedEndec<>("duration", Endec.INT, 0);
}

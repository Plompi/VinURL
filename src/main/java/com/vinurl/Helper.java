package com.vinurl;

import com.vinurl.items.VinURLDiscItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

//? if <1.20.5
/*import net.fabricmc.fabric.api.item.v1.FabricItemSettings;*/
//? if >=1.20.5 {
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
//?}

import static com.vinurl.VinURL.PLACEHOLDER_SOUND_IDENTIFIER;
import static com.vinurl.VinURL.URL_KEY;

public class Helper {
	public static Identifier identifier(String modid, String name){
		//? if <1.20.5 {
		/*return new Identifier(modid, name);
		 *///?} else {
		return Identifier.of(modid, name);
		//?}
	}

	public static void setNbt(ItemStack item, NbtCompound nbt){
		//? if <1.20.5 {
		/*item.setNbt(nbt);
		*///?} else {
		item.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
		//?}
	}

	public static NbtCompound getNbt(ItemStack item){
		//? if <1.20.5 {
		/*return item.getOrCreateNbt();
		 *///?} else {
		NbtCompound currentNbt = new NbtCompound();
		currentNbt.put(URL_KEY, "");
		return item.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(currentNbt)).copyNbt();
		//?}
	}

	public static void playSound(PlayerEntity player, SoundEvent event, SoundCategory category, float volume, float pitch){
		//? if <1.20.5 {
		/*player.playSound(event, category, volume, pitch);
		*///?} else {
		player.playSoundToPlayer(event, category, volume, pitch);
		//?}
	}

	public static Item getRecord() {
		SoundEvent PLACEHOLDER_SOUND = Registry.register(
				Registries.SOUND_EVENT,
				PLACEHOLDER_SOUND_IDENTIFIER,
				SoundEvent.of(PLACEHOLDER_SOUND_IDENTIFIER));

		//? if <1.20.5 {
		/*return Registry.register(
				Registries.ITEM,
				identifier(VinURL.MOD_ID, "custom_record"),
				new VinURLDiscItem(17, PLACEHOLDER_SOUND, new FabricItemSettings().maxCount(1), 0));
		*///?} else {
		RegistryKey<JukeboxSong> Song = RegistryKey.of(RegistryKeys.JUKEBOX_SONG, PLACEHOLDER_SOUND_IDENTIFIER);
		return Registry.register(Registries.ITEM, identifier(VinURL.MOD_ID, "custom_record"), new VinURLDiscItem(new Item.Settings().maxCount(1).jukeboxPlayable(Song)));
		//?}
	}
}

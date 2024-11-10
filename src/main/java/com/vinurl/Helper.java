package com.vinurl;

import com.vinurl.items.VinURLDiscItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import static com.vinurl.VinURL.PLACEHOLDER_SOUND;

public class Helper {
	public static Identifier identifier(String modid, String name){
		//? if <1.20.5 {
		return new Identifier(modid, name);
		 //?} else
		/*return Identifier.of(modid, name);*/
	}

	public static void playSound(PlayerEntity player, SoundEvent event, SoundCategory category, float volume, float pitch){
		//? if <1.20.5 {
		player.playSound(SoundEvents.ENTITY_VILLAGER_WORK_CARTOGRAPHER, SoundCategory.BLOCKS, 1.0f, 1.0f);
		//?} else
		/*player.playSoundToPlayer(SoundEvents.ENTITY_VILLAGER_WORK_CARTOGRAPHER, SoundCategory.BLOCKS, 1.0f, 1.0f);*/
	}

	public static void setNbt(ItemStack currentItem, NbtCompound nbt){
		//? if <1.20.5 {
		currentItem.setNbt(nbt);
		//?} else
		/*currentItem.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(currentNbt));*/
	}

	public static Item getRecord(){
		//? if <1.20.5 {
		return Registry.register(
				Registries.ITEM,
				new Identifier(VinURL.MOD_ID, "custom_record"),
				new VinURLDiscItem(17, PLACEHOLDER_SOUND, new FabricItemSettings().maxCount(1), 0));
		//?} else
		/*
		* RegistryKey<JukeboxSong> Song = RegistryKey.of(RegistryKeys.JUKEBOX_SONG, PLACEHOLDER_SOUND_IDENTIFIER);
		* return Registry.register(Registries.ITEM, identifier(MOD_ID, "custom_record"), new VinURLDiscItem(new Item.Settings().maxCount(1).jukeboxPlayable(Song)));*/

	}
}

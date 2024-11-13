package com.vinurl;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

import static com.vinurl.VinURL.URL_KEY;

public class Helper {
	public static Identifier identifier(String modid, String name){
		//? if <1.20.5 {
		/*return new Identifier(modid, name);
		 *///?} else
		return Identifier.of(modid, name);
	}

	public static void setNbt(ItemStack item, NbtCompound nbt){
		//? if <1.20.5 {
		/*item.setNbt(nbt);
		*///?} else
		item.set(net.minecraft.component.DataComponentTypes.CUSTOM_DATA, net.minecraft.component.type.NbtComponent.of(nbt));
	}

	public static NbtCompound getNbt(ItemStack item){
		//? if <1.20.5 {
		/*return item.getOrCreateNbt();
		 *///?} else {
		NbtCompound currentNbt = new NbtCompound();
		currentNbt.put(URL_KEY, "");
		return item.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA, net.minecraft.component.type.NbtComponent.of(currentNbt)).copyNbt();
		//?}
	}

	public static void playSound(PlayerEntity player, SoundEvent event, SoundCategory category, float volume, float pitch){
		//? if <1.20.5 {
		/*player.playSound(event, category, volume, pitch);
		*///?} else
		player.playSoundToPlayer(event, category, volume, pitch);
	}
}

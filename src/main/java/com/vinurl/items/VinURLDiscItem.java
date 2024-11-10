package com.vinurl.items;

import com.vinurl.VinURL;
//? if >=1.20.5 {
/*import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
*///?}

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import static com.vinurl.VinURL.NETWORK_CHANNEL;

//? if >=1.20.5 {
/*public class VinURLDiscItem extends Item {

	public VinURLDiscItem(Item.Settings settings) {
		super(settings);
	}

	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stackInHand = player.getStackInHand(hand);
		if (!world.isClient) {

			NbtCompound currentNbt = new NbtCompound();
			currentNbt.putString("music_url", "");

			NbtCompound value = stackInHand.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(currentNbt)).copyNbt();
			NETWORK_CHANNEL.serverHandle(player).send(new VinURL.GUIRecord(value.getString("music_url")));
		}
		return TypedActionResult.success(stackInHand);
	}
}
*///?} else

public class VinURLDiscItem extends MusicDiscItem {
	public VinURLDiscItem(int comparatorOutput, SoundEvent sound, Settings settings, int lengthInSeconds) {
		super(comparatorOutput, sound, settings, lengthInSeconds);
	}

	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stackInHand = player.getStackInHand(hand);
		if (!world.isClient) {
			NbtCompound currentNbt = new NbtCompound();
			currentNbt.putString("music_url", "");

			NbtCompound value = stackInHand.getOrCreateNbt();
			NETWORK_CHANNEL.serverHandle(player).send(new VinURL.GUIRecord(value.getString("music_url")));
		}
		return TypedActionResult.success(stackInHand);
	}
}





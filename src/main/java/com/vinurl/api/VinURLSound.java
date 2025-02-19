package com.vinurl.api;

import static com.vinurl.VinURL.*;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VinURLSound {
	public static void play(World world, ItemStack stack, BlockPos position) {
		NbtComponent nbt = stack.get(DataComponentTypes.CUSTOM_DATA);
		if (stack.getItem() != CUSTOM_RECORD || world.isClient || nbt == null) {return;}

		String url = nbt.copyNbt().get(URL_KEY);
		Boolean loop = nbt.copyNbt().get(LOOP_KEY);
		if (url == null || url.isEmpty()) {return;}

		for (PlayerEntity player : world.getPlayers()) {
			NETWORK_CHANNEL.serverHandle(player).send(new PlaySoundRecord(position, url, loop));
		}
	}

	public static void stop(World world, ItemStack stack, BlockPos position) {
		if (stack.getItem() != CUSTOM_RECORD || world.isClient) {return;}
		for (PlayerEntity player : world.getPlayers()) {
			NETWORK_CHANNEL.serverHandle(player).send(new PlaySoundRecord(position, "", false));
		}
	}
}
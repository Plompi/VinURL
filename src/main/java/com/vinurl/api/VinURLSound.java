package com.vinurl.api;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static com.vinurl.util.Constants.*;
import static com.vinurl.util.Networking.*;

public class VinURLSound {
	private static final byte JUKEBOX_RANGE = 64;

	public static void play(World world, ItemStack stack, BlockPos position) {
		NbtComponent nbt = stack.get(DataComponentTypes.CUSTOM_DATA);
		if (stack.getItem() != CUSTOM_RECORD || world.isClient || nbt == null) {return;}

		for (PlayerEntity player : world.getPlayers()) {
			if (player.getPos().distanceTo(position.toCenterPos()) <= JUKEBOX_RANGE) {
				NETWORK_CHANNEL.serverHandle(player).send(new PlaySoundRecord(position, nbt.copyNbt().get(URL_KEY), nbt.copyNbt().get(LOOP_KEY)));
			}
		}
	}

	public static void stop(World world, ItemStack stack, BlockPos position) {
		if (stack.getItem() != CUSTOM_RECORD || world.isClient) {return;}
		for (PlayerEntity player : world.getPlayers()) {
			NETWORK_CHANNEL.serverHandle(player).send(new PlaySoundRecord(position, "",false));
		}
	}
}
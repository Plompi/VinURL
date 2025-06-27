package com.vinurl.api;

import com.vinurl.net.ClientEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static com.vinurl.util.Constants.*;

public class VinURLSound {
	private static final int JUKEBOX_RANGE = 64;

	public static void play(World world, ItemStack stack, BlockPos position) {
		if (world == null || world.isClient) {return;}
		NbtCompound nbt = stack.getOrCreateNbt();
		for (PlayerEntity player : world.getPlayers()) {
			if (player.getPos().distanceTo(position.toCenterPos()) <= JUKEBOX_RANGE) {
				NETWORK_CHANNEL.serverHandle(player).send(new ClientEvent.PlaySoundRecord(position, nbt.getString(URL_KEY), nbt.getBoolean(LOOP_KEY)));
			}
		}
	}

	public static void stop(World world, ItemStack stack, BlockPos position, boolean cancel) {
		if (world == null || world.isClient) {return;}
		NbtCompound nbt = stack.getOrCreateNbt();
		for (PlayerEntity player : world.getPlayers()) {
			NETWORK_CHANNEL.serverHandle(player).send(new ClientEvent.StopSoundRecord(position, nbt.getString(URL_KEY), cancel));
		}
	}
}
package com.vinurl.api;

import com.vinurl.net.ClientEvent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import static com.vinurl.util.Constants.*;

public class VinURLSound {
	private static final int JUKEBOX_RANGE = 64;

	public static void play(ServerWorld world, ItemStack stack, BlockPos position) {
		NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
		for (PlayerEntity player : world.getPlayers()) {
			if (player.getPos().distanceTo(position.toCenterPos()) <= JUKEBOX_RANGE) {
				NETWORK_CHANNEL.serverHandle(player).send(new ClientEvent.PlaySoundRecord(position, nbt.get(URL_KEY), nbt.get(LOOP_KEY)));
			}
		}
	}

	public static void stop(ServerWorld world, ItemStack stack, BlockPos position, boolean cancel) {
		NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
		for (PlayerEntity player : world.getPlayers()) {
			NETWORK_CHANNEL.serverHandle(player).send(new ClientEvent.StopSoundRecord(position, nbt.get(URL_KEY), cancel));
		}
	}
}
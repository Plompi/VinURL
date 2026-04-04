package com.vinurl.api;

import com.vinurl.net.ClientEvent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

import static com.vinurl.util.Constants.*;

public class VinURLSound {
	private static final int JUKEBOX_RANGE = 64;
	private static final Map<BlockPos, Set<UUID>> listeners = new HashMap<>();

	public static void play(World world, ItemStack stack, BlockPos position, long ticks) {
		if (world == null || world.isClient) {return;}
		NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
		Set<UUID> set = listeners.computeIfAbsent(position, pos -> new HashSet<>());
		for (PlayerEntity player : world.getPlayers()) {
			if (player.getPos().distanceTo(position.toCenterPos()) <= JUKEBOX_RANGE && !listeners.get(position).contains(player.getUuid())) {
				set.add(player.getUuid());
				NETWORK_CHANNEL.serverHandle(player).send(new ClientEvent.PlaySoundRecord(position, nbt.get(URL_KEY), ticks, nbt.get(LOOP_KEY)));
			}
		}
	}

	public static void stop(World world, ItemStack stack, BlockPos position, boolean cancel) {
		if (world == null || world.isClient) {return;}
		NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
		Set<UUID> set = listeners.get(position);
		for (PlayerEntity player : world.getPlayers()) {
			if (set != null) {
				set.remove(player.getUuid());

				if (set.isEmpty()) {
					listeners.remove(position);
				}
			}
			NETWORK_CHANNEL.serverHandle(player).send(new ClientEvent.StopSoundRecord(position, nbt.get(URL_KEY), cancel));
		}
	}
}
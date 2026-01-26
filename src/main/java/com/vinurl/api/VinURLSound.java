package com.vinurl.api;

import com.vinurl.net.ClientEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static com.vinurl.util.Constants.*;

@SuppressWarnings("unused")
public class VinURLSound {
	private static final double JUKEBOX_RANGE = 64;
	private static final double INFINITE_RANGE = Double.POSITIVE_INFINITY;

	public static void playAt(Level level, ItemStack stack, BlockPos pos) {
		send(level, stack, playersInRange(level, pos, JUKEBOX_RANGE), (tag) ->
			new ClientEvent.PlaySoundRecord(pos, tag.get(URL_KEY), tag.get(LOOP_KEY))
		);
	}

	public static void playFor(Level level, ItemStack stack, UUID uuid) {
		send(level, stack, playerByUuid(level, uuid), (tag) ->
			new ClientEvent.PlaySoundRecord(null, tag.get(URL_KEY), tag.get(LOOP_KEY))
		);
	}

	public static void stopAt(Level level, ItemStack stack, BlockPos pos, boolean cancelable) {
		send(level, stack, playersInRange(level, pos, INFINITE_RANGE), (tag) ->
			new ClientEvent.StopSoundRecord(pos, tag.get(URL_KEY), cancelable)
		);
	}

	public static void stopFor(Level level, ItemStack stack, UUID uuid, boolean cancelable) {
		send(level, stack, playerByUuid(level, uuid), (tag) ->
			new ClientEvent.StopSoundRecord(null, tag.get(URL_KEY), cancelable)
		);
	}

	private static void send(Level level, ItemStack stack, Iterable<Player> players, Function<CompoundTag, Record> factory) {
		if (level == null || level.isClientSide()) {return;}

		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

		for (Player player : players) {
			NETWORK_CHANNEL.serverHandle(player).send(factory.apply(tag));
		}
	}

	private static Iterable<Player> playersInRange(Level level, BlockPos pos, double range) {
		return level.players().stream()
			.filter((player) -> player.position().distanceTo(pos.getCenter()) <= range)
			.map((player) -> (Player) player).toList();
	}

	private static Iterable<Player> playerByUuid(Level level, UUID uuid) {
		Player player = level.getPlayerByUUID(uuid);
		return player != null ? List.of(player) : List.of();
	}
}
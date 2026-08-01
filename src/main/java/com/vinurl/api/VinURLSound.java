package com.vinurl.api;

import com.vinurl.net.ClientEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.vinurl.VinURL.CUSTOM_RECORD;
import static com.vinurl.util.Constants.NETWORK_CHANNEL;
import static com.vinurl.util.Constants.URL_KEY;

@SuppressWarnings("unused")
public class VinURLSound {
	private static final double JUKEBOX_RANGE = 64;
	private static final double INFINITE_RANGE = Double.POSITIVE_INFINITY;

	public static void playAt(ServerLevel level, ItemStack stack, BlockPos pos) {
		send(stack, () -> playersInRange(level, pos, JUKEBOX_RANGE), (tag) ->
			new ClientEvent.PlaySoundRecord(pos, -1, tag.get(URL_KEY))
		);
	}

	public static void playFor(ServerLevel level, ItemStack stack, Entity entity) {
		send(stack, () -> playersInRange(level, entity, JUKEBOX_RANGE), (tag) ->
			new ClientEvent.PlaySoundRecord(null, entity.getId(), tag.get(URL_KEY))
		);
	}

	public static void stopAt(ServerLevel level, ItemStack stack, BlockPos pos, boolean cancelable) {
		send(stack, () -> playersInRange(level, pos, INFINITE_RANGE), (tag) ->
			new ClientEvent.StopSoundRecord(pos, -1, tag.get(URL_KEY), cancelable)
		);
	}

	public static void stopFor(ServerLevel level, ItemStack stack, Entity entity, boolean cancelable) {
		send(stack, () -> playersInRange(level, entity, INFINITE_RANGE), (tag) ->
			new ClientEvent.StopSoundRecord(null, entity.getId(), tag.get(URL_KEY), cancelable)
		);
	}

	private static void send(ItemStack stack, Supplier<List<ServerPlayer>> players, Function<CompoundTag, Record> factory) {
		if (!stack.is(CUSTOM_RECORD)) {return;}

		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		for (ServerPlayer player : players.get()) {
			NETWORK_CHANNEL.serverHandle(player).send(factory.apply(tag));
		}
	}

	private static List<ServerPlayer> playersInRange(ServerLevel level, BlockPos pos, double range) {
		return level.getPlayers((player) -> player.position().distanceTo(pos.getCenter()) <= range);
	}

	private static List<ServerPlayer> playersInRange(ServerLevel level, Entity entity, double range) {
		return playersInRange(level, entity.blockPosition(), range);
	}
}
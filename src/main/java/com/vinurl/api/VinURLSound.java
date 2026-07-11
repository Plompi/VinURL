package com.vinurl.api;

import com.vinurl.net.ClientEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.vinurl.VinURL.CUSTOM_RECORD;
import static com.vinurl.util.Constants.*;

@SuppressWarnings("unused")
public class VinURLSound {
	private static final double JUKEBOX_RANGE = 64;
	private static final double INFINITE_RANGE = Double.POSITIVE_INFINITY;

	public static void playAt(ServerLevel level, ItemStack stack, BlockPos pos) {
		send(stack, () -> playersInRange(level, pos, JUKEBOX_RANGE), (tag) ->
			new ClientEvent.PlaySoundRecord(pos, -1, tag.get(URL_KEY), tag.get(LOOP_KEY))
		);
	}

	public static void playFor(ServerLevel level, ItemStack stack, int entityID) {
		send(stack, () -> playersInRange(level, entityID, JUKEBOX_RANGE), (tag) ->
			new ClientEvent.PlaySoundRecord(null, entityID, tag.get(URL_KEY), tag.get(LOOP_KEY))
		);
	}

	public static void stopAt(ServerLevel level, ItemStack stack, BlockPos pos, boolean cancelable) {
		send(stack, () -> playersInRange(level, pos, INFINITE_RANGE), (tag) ->
			new ClientEvent.StopSoundRecord(pos, -1, tag.get(URL_KEY), cancelable)
		);
	}

	public static void stopFor(ServerLevel level, ItemStack stack, int entityID, boolean cancelable) {
		send(stack, () -> playersInRange(level, entityID, INFINITE_RANGE), (tag) ->
			new ClientEvent.StopSoundRecord(null, entityID, tag.get(URL_KEY), cancelable)
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

	private static List<ServerPlayer> playersInRange(ServerLevel level, int entityID, double range) {
		return Optional.ofNullable(level.getEntity(entityID))
			.map(entity -> playersInRange(level, entity.blockPosition(), range))
			.orElse(List.of());
	}
}
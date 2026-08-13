package com.vinurl.api;

import com.vinurl.component.AudioComponent;
import com.vinurl.net.packet.PlaySoundPacket;
import com.vinurl.net.packet.StopSoundPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.vinurl.VinURL.AUDIO_COMPONENT;
import static com.vinurl.VinURL.CUSTOM_RECORD;

@SuppressWarnings("unused")
public class VinURLSound {
	private static final double JUKEBOX_RANGE = 64;
	private static final double INFINITE_RANGE = Double.POSITIVE_INFINITY;

	public static void playAt(ServerLevel level, ItemStack stack, BlockPos pos) {
		send(stack, () -> playersInRange(level, pos, JUKEBOX_RANGE), (component) ->
			new PlaySoundPacket(pos, -1, component.url())
		);
	}

	public static void playFor(ServerLevel level, ItemStack stack, Entity entity) {
		send(stack, () -> playersInRange(level, entity, JUKEBOX_RANGE), (component) ->
			new PlaySoundPacket(null, entity.getId(), component.url())
		);
	}

	public static void stopAt(ServerLevel level, ItemStack stack, BlockPos pos, boolean cancelable) {
		send(stack, () -> playersInRange(level, pos, INFINITE_RANGE), (component) ->
			new StopSoundPacket(pos, -1, component.url(), cancelable)
		);
	}

	public static void stopFor(ServerLevel level, ItemStack stack, Entity entity, boolean cancelable) {
		send(stack, () -> playersInRange(level, entity, INFINITE_RANGE), (component) ->
			new StopSoundPacket(null, entity.getId(), component.url(), cancelable)
		);
	}

	private static void send(ItemStack stack, Supplier<List<ServerPlayer>> players, Function<AudioComponent, CustomPacketPayload> factory) {
		if (!stack.is(CUSTOM_RECORD)) {return;}

		AudioComponent component = stack.getOrDefault(AUDIO_COMPONENT, AudioComponent.DEFAULT);
		for (ServerPlayer player : players.get()) {
			ServerPlayNetworking.send(player, factory.apply(component));
		}
	}

	private static List<ServerPlayer> playersInRange(ServerLevel level, BlockPos pos, double range) {
		return level.getPlayers((player) -> player.position().distanceTo(pos.getCenter()) <= range);
	}

	private static List<ServerPlayer> playersInRange(ServerLevel level, Entity entity, double range) {
		return playersInRange(level, entity.blockPosition(), range);
	}
}
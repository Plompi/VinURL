package com.vinurl.api;

import com.vinurl.net.ClientEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.function.Function;

import static com.vinurl.util.Constants.*;

public class VinURLSound {
	private static final double JUKEBOX_RANGE = 64;
	private static final double INFINITE_RANGE = Double.POSITIVE_INFINITY;

	public static void play(Level world, ItemStack stack, BlockPos position) {
		sendToPlayers(world, stack, position, JUKEBOX_RANGE, (tag) ->
			new ClientEvent.PlaySoundRecord(position, tag.get(URL_KEY), tag.get(LOOP_KEY))
		);
	}

	public static void stop(Level world, ItemStack stack, BlockPos position, boolean cancel) {
		sendToPlayers(world, stack, position, INFINITE_RANGE, (tag) ->
			new ClientEvent.StopSoundRecord(position, tag.get(URL_KEY), cancel)
		);
	}

	private static void sendToPlayers(Level level, ItemStack stack, BlockPos pos, double range, Function<CompoundTag, Record> factory) {
		if (level == null || level.isClientSide) {return;}
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		for (Player player : level.players()) {
			if (player.position().distanceTo(pos.getCenter()) <= range) {
				NETWORK_CHANNEL.serverHandle(player).send(factory.apply(tag));
			}
		}
	}
}
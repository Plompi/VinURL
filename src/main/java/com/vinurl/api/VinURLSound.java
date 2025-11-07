package com.vinurl.api;

import com.vinurl.net.ClientEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import static com.vinurl.util.Constants.*;

public class VinURLSound {
	private static final int JUKEBOX_RANGE = 64;

	public static void play(Level level, ItemStack stack, BlockPos pos) {
		if (level == null || level.isClientSide) {return;}
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		for (Player player : level.players()) {
			if (player.position().distanceTo(pos.getCenter()) <= JUKEBOX_RANGE) {
				NETWORK_CHANNEL.serverHandle(player).send(new ClientEvent.PlaySoundRecord(pos, tag.get(URL_KEY), tag.get(LOOP_KEY)));
			}
		}
	}

	public static void stop(Level level, ItemStack stack, BlockPos pos, boolean cancel) {
		if (level == null || level.isClientSide) {return;}
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		for (Player player : level.players()) {
			NETWORK_CHANNEL.serverHandle(player).send(new ClientEvent.StopSoundRecord(pos, tag.get(URL_KEY), cancel));
		}
	}
}
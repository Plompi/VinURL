package com.vinurl.api;

import com.vinurl.VinURL;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static com.vinurl.VinURL.*;

public class VinURLSound {
	public static void play(World level, ItemStack recordStack, BlockPos pos) {
		NbtComponent nbt = recordStack.get(DataComponentTypes.CUSTOM_DATA);
		if (recordStack.getItem() != CUSTOM_RECORD || level.isClient || nbt == null) {return;}

		String musicUrl = nbt.copyNbt().get(URL_KEY);
		if (musicUrl == null || musicUrl.isEmpty()) {return;}

		level.getPlayers().forEach(playerEntity -> {
			NETWORK_CHANNEL.serverHandle(playerEntity).send(new VinURL.PlaySoundRecord(pos, musicUrl));
		});
	}

	public static void stop(World level, ItemStack recordStack, BlockPos pos) {
		if (recordStack.getItem() != CUSTOM_RECORD || level.isClient) {return;}
		level.getPlayers().forEach(playerEntity -> {
			NETWORK_CHANNEL.serverHandle(playerEntity).send(new VinURL.PlaySoundRecord(pos, ""));
		});
	}
}
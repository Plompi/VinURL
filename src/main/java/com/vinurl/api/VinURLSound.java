package com.vinurl.api;

import com.vinurl.VinURL;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static com.vinurl.VinURL.*;

public class VinURLSound {
	public static void play(World world, ItemStack stack, BlockPos position) {
		NbtComponent nbt = stack.get(DataComponentTypes.CUSTOM_DATA);
		if (stack.getItem() != CUSTOM_RECORD || world.isClient || nbt == null) {return;}

		String url = nbt.copyNbt().get(URL_KEY);
		if (url == null || url.isEmpty()) {return;}

		world.getPlayers().forEach(playerEntity -> {
			NETWORK_CHANNEL.serverHandle(playerEntity).send(new VinURL.PlaySoundRecord(position, url));
		});
	}

	public static void stop(World world, ItemStack stack, BlockPos position) {
		if (stack.getItem() != CUSTOM_RECORD || world.isClient) {return;}
		world.getPlayers().forEach(playerEntity -> {
			NETWORK_CHANNEL.serverHandle(playerEntity).send(new VinURL.PlaySoundRecord(position, ""));
		});
	}
}
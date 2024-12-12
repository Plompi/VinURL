package com.vinurl.api;

import com.vinurl.VinURL;
import com.vinurl.items.VinURLDiscItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static com.vinurl.VinURL.NETWORK_CHANNEL;

public class VinURLSound {
	public static void play(World level, ItemStack recordStack, BlockPos pos) {
		NbtComponent nbt = recordStack.get(DataComponentTypes.CUSTOM_DATA);
		if (recordStack.getItem() instanceof VinURLDiscItem && !level.isClient && nbt != null) {
			String musicUrl = nbt.copyNbt().getString("music_url");
			if (musicUrl != null && !musicUrl.isEmpty()) {
				level.getPlayers().forEach(playerEntity -> {
					NETWORK_CHANNEL.serverHandle(playerEntity).send(new VinURL.PlaySoundRecord(pos, musicUrl));
				});
			}
		}
	}

	public static void stop(World level, BlockPos pos) {
		level.getPlayers().forEach(playerEntity -> {
			NETWORK_CHANNEL.serverHandle(playerEntity).send(new VinURL.PlaySoundRecord(pos, ""));
		});
	}
}
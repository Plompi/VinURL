package com.vinurl;

import com.vinurl.items.VinURLDiscItem;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Objects;

public class Helper {
	public static void playVinURLDisc(World level, ItemStack recordStack, BlockPos pos) {
		if (recordStack.getItem() instanceof VinURLDiscItem && !level.isClient) {
			String musicUrl = Objects.requireNonNull(recordStack.get(DataComponentTypes.CUSTOM_DATA)).copyNbt().getString("music_url");
			if (musicUrl != null && !musicUrl.isEmpty()) {
				level.getPlayers().forEach(
						playerEntity -> ServerPlayNetworking.send(
								(ServerPlayerEntity) playerEntity,
								new VinURL.PlaySoundPayload(pos, musicUrl)
						)
				);
			}
		}
	}

	public static void stopVinURLDisc(World level, BlockPos pos) {
		level.getPlayers().forEach(playerEntity -> {
			ServerPlayNetworking.send((ServerPlayerEntity) playerEntity, new VinURL.PlaySoundPayload(pos, ""));
		});
	}
}
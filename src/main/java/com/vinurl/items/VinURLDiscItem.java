package com.vinurl.items;

import com.vinurl.VinURL;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class VinURLDiscItem extends Item {

	public VinURLDiscItem(Item.Settings settings) {
		super(settings);
	}

	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stackInHand = player.getStackInHand(hand);
		if (!world.isClient) {

			NbtCompound currentNbt = new NbtCompound();
			currentNbt.putString("music_url", "");

			NbtCompound value = stackInHand.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(currentNbt)).copyNbt();
			ServerPlayNetworking.send((ServerPlayerEntity) player, new VinURL.RecordGUIPayload(value.getString("music_url")));
		}
		return TypedActionResult.success(stackInHand);
	}
}
package com.vinurl.items;

import com.vinurl.VinURL;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import static com.vinurl.Helper.getNbt;
import static com.vinurl.VinURL.NETWORK_CHANNEL;
import static com.vinurl.VinURL.URL_KEY;

public class VinURLDiscItem extends Item {

	public VinURLDiscItem(Item.Settings settings) {
		super(settings);
	}

	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stackInHand = player.getStackInHand(hand);
		if (!world.isClient) {

			NbtCompound nbt = getNbt(stackInHand);
			NETWORK_CHANNEL.serverHandle(player).send(new VinURL.GUIRecord(nbt.get(URL_KEY)));
		}
		return TypedActionResult.success(stackInHand);
	}
}
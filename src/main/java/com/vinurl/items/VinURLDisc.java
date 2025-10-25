package com.vinurl.items;

import com.vinurl.net.ClientEvent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import static com.vinurl.util.Constants.*;

public class VinURLDisc extends Item {

	public VinURLDisc() {
		super(new Item.Settings()
			.maxCount(1)
			.rarity(Rarity.RARE)
			.jukeboxPlayable(SONG));
	}

	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (!world.isClient) {
			NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
			if (!nbt.get(LOCK_KEY)) {
				NETWORK_CHANNEL.serverHandle(player).send(new ClientEvent.GUIRecord(nbt.get(URL_KEY), nbt.get(DURATION_KEY), nbt.get(LOOP_KEY)));
			} else {
				player.sendMessage(Text.literal("Locked 🔒"), true);
			}
		}
		return TypedActionResult.success(stack);
	}
}
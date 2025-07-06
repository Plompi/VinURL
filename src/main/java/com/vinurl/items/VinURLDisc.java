package com.vinurl.items;

import com.vinurl.net.ClientEvent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.JukeboxPlayableComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryPair;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

import static com.vinurl.util.Constants.*;

public class VinURLDisc extends Item {

	public VinURLDisc() {
		super(new Item.Settings()
			.maxCount(1)
			.rarity(Rarity.RARE)
			.component(DataComponentTypes.JUKEBOX_PLAYABLE, new JukeboxPlayableComponent(new RegistryPair<>(SONG), false)));
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

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		NbtComponent nbt = stack.get(DataComponentTypes.CUSTOM_DATA);
		if (nbt == null) {return;}

		tooltip.add(Text.translatable("itemGroup.tools").formatted(Formatting.BLUE));
		if (nbt.copyNbt().get(LOCK_KEY)) {
			tooltip.add(Text.literal("Locked 🔒").formatted(Formatting.GRAY));
		}
	}
}
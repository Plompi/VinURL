package com.vinurl.items;

import com.vinurl.net.ClientEvent;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.vinurl.util.Constants.*;

public class VinURLDisc extends MusicDiscItem {

	public VinURLDisc() {
		super(15, SONG, new FabricItemSettings().maxCount(1).rarity(Rarity.RARE), 3600);
	}

	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (!world.isClient) {
			NbtCompound nbt = stack.getOrCreateNbt();
			if (!nbt.getBoolean(LOCK_KEY)) {
				NETWORK_CHANNEL.serverHandle(player).send(new ClientEvent.GUIRecord(nbt.getString(URL_KEY), nbt.getInt(DURATION_KEY), nbt.getBoolean(LOOP_KEY)));
			} else {
				player.sendMessage(Text.literal("Locked 🔒"), true);
			}
		}
		return TypedActionResult.success(stack);
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		NbtCompound nbt = stack.getOrCreateNbt();
		if (nbt.equals(new NbtCompound())) {return;}

		tooltip.add(Text.translatable("itemGroup.tools").formatted(Formatting.BLUE));
		if (nbt.getBoolean(LOCK_KEY)) {
			tooltip.add(Text.literal("Locked 🔒").formatted(Formatting.GRAY));
		}
	}
}
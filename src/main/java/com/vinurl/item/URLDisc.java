package com.vinurl.item;

import com.vinurl.net.ClientEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import static com.vinurl.util.Constants.*;

public class URLDisc extends Item {

	public URLDisc() {
		super(new Item.Properties()
			.stacksTo(1)
			.rarity(Rarity.RARE)
			.jukeboxPlayable(SONG_KEY)
			.setId(ITEM_KEY));
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide()) {
			CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			if (!tag.get(LOCK_KEY)) {
				NETWORK_CHANNEL.serverHandle(player).send(new ClientEvent.GUIRecord(tag.get(URL_KEY), tag.get(DURATION_KEY), tag.get(LOOP_KEY)));
			} else {
				player.displayClientMessage(Component.translatable("item.vinurl.custom_record.message.locked"), true);
			}
		}
		return InteractionResult.SUCCESS;
	}
}
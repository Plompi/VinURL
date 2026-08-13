package com.vinurl.item;

import com.vinurl.component.AudioComponent;
import com.vinurl.net.packet.GUIPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

import static com.vinurl.VinURL.AUDIO_COMPONENT;
import static com.vinurl.util.Constants.SONG_KEY;

public class URLDisc extends Item {

	public URLDisc() {
		super(new Item.Properties()
			.stacksTo(1)
			.rarity(Rarity.RARE)
			.jukeboxPlayable(SONG_KEY));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide()) {
			AudioComponent component = stack.getOrDefault(AUDIO_COMPONENT, AudioComponent.DEFAULT);
			if (!component.lock()) {
				ServerPlayNetworking.send((ServerPlayer) player, new GUIPacket(component.url(), component.duration()));
			} else {
				player.displayClientMessage(Component.translatable("item.vinurl.custom_record.message.locked"), true);
			}
		}
		return InteractionResultHolder.success(stack);
	}
}
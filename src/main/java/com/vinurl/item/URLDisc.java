package com.vinurl.item;

import com.vinurl.net.ClientEvent;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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

	public static final class DataWrapper {
		public static final String URL_KEY = "music_url";
		public static final String LOCK_KEY = "lock";
		public static final String DURATION_KEY = "duration";

		public static void putUrl(CompoundTag tag, String url) {
			tag.put(URL_KEY, StringTag.valueOf(url));
		}

		public static void putLock(CompoundTag tag, boolean lock) {
			tag.putBoolean(LOCK_KEY, lock);
		}

		public static void putDuration(CompoundTag tag, int duration) {
			tag.putInt(DURATION_KEY, duration);
		}

		public static boolean hasDuration(CompoundTag tag) {
			return tag.getInt(DURATION_KEY).isPresent();
		}

		public static String getUrl(CompoundTag tag) {
			return tag.getStringOr(URL_KEY, "");
		}

		public static boolean getLock(CompoundTag tag) {
			return tag.getBooleanOr(LOCK_KEY, false);
		}

		public static int getDuration(CompoundTag tag) {
			return tag.getIntOr(DURATION_KEY, 0);
		}
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide()) {
			CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			if (!DataWrapper.getLock(tag)) {
				ServerPlayNetworking.send((ServerPlayer) player, new ClientEvent.GUIRecord(DataWrapper.getUrl(tag), DataWrapper.getDuration(tag)));
			} else {
				player.sendOverlayMessage(Component.translatable("item.vinurl.custom_record.message.locked"));
			}
		}
		return InteractionResult.SUCCESS;
	}
}

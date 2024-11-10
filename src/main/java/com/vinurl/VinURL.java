package com.vinurl;

import io.wispforest.owo.network.OwoNetChannel;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.loader.api.FabricLoader;
//? if >=1.20.5 {
/*import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
*///?}
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Path;

import static com.vinurl.Helper.*;


public class VinURL implements ModInitializer {
	public static final String MOD_ID = "vinurl";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Path VINURLPATH = FabricLoader.getInstance().getGameDir().resolve(MOD_ID);
	public static final OwoNetChannel NETWORK_CHANNEL = OwoNetChannel.create(identifier(MOD_ID, "main"));
	public static final Identifier PLACEHOLDER_SOUND_IDENTIFIER = identifier(MOD_ID, "placeholder_sound");
	public static final SoundEvent PLACEHOLDER_SOUND = Registry.register(
			Registries.SOUND_EVENT,
			PLACEHOLDER_SOUND_IDENTIFIER,
			SoundEvent.of(PLACEHOLDER_SOUND_IDENTIFIER)
	);

	public static final Item CUSTOM_RECORD = getRecord();

	@Override
	public void onInitialize() {
		// Register the Custom Record to the Tools Item Group
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register((content) -> {
			content.add(CUSTOM_RECORD);
		});


		// Server event handler for setting the URL on the Custom Record
		NETWORK_CHANNEL.registerServerbound(SetURLRecord.class,((payload, context) -> {
			PlayerEntity player = context.player();
			ItemStack currentItem = player.getStackInHand(player.getActiveHand());

			if (currentItem.getItem() != CUSTOM_RECORD) {
				return;
			}

			String urlName = payload.urlName();

			try {
				new URI(urlName).toURL();

			} catch (Exception e) {
				player.sendMessage(Text.literal("Song URL is invalid!"));
				return;
			}

			if (urlName.length() >= 400) {
				player.sendMessage(Text.literal("Song URL is too long!"));
				return;
			}

			playSound(player, SoundEvents.ENTITY_VILLAGER_WORK_CARTOGRAPHER, SoundCategory.BLOCKS, 1.0f, 1.0f);
			NbtCompound currentNbt = new NbtCompound();
			currentNbt.putString("music_url", urlName);
			setNbt(currentItem, currentNbt);
		}));
	}

	public record PlaySoundRecord(BlockPos blockPos, String urlName) {}
	public record SetURLRecord(String urlName) {}
	public record GUIRecord(String urlName) {}
}
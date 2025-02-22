package com.vinurl;

import com.vinurl.items.VinURLDiscItem;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.KeyedEndec;
import io.wispforest.owo.network.OwoNetChannel;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.JukeboxPlayableComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;

public class VinURL implements ModInitializer {
	public static final String MOD_ID = "vinurl";
	public static final KeyedEndec<String> URL_KEY = new KeyedEndec<>("music_url", Endec.STRING, "");
	public static final KeyedEndec<Boolean> LOOP_KEY = new KeyedEndec<>("loop", Endec.BOOLEAN, false);
	public static final OwoNetChannel NETWORK_CHANNEL = OwoNetChannel.create(Identifier.of(MOD_ID, "main"));
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Path VINURLPATH = FabricLoader.getInstance().getGameDir().resolve(MOD_ID);
	public static final Identifier PLACEHOLDER_SOUND_IDENTIFIER = Identifier.of(MOD_ID, "placeholder_sound");
	public static final RegistryKey<JukeboxSong> SONG = RegistryKey.of(RegistryKeys.JUKEBOX_SONG, PLACEHOLDER_SOUND_IDENTIFIER);
	public static final Item CUSTOM_RECORD = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "custom_record"), new VinURLDiscItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE).component(DataComponentTypes.JUKEBOX_PLAYABLE, new JukeboxPlayableComponent(new RegistryPair<>(SONG), false))));

	@Override
	public void onInitialize() {
		// Register the Custom Record to the Tools Item Group
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register((content) -> content.add(CUSTOM_RECORD));

		Registry.register(
				Registries.SOUND_EVENT,
				PLACEHOLDER_SOUND_IDENTIFIER,
				SoundEvent.of(PLACEHOLDER_SOUND_IDENTIFIER));

		NETWORK_CHANNEL.registerClientboundDeferred(GUIRecord.class);
		NETWORK_CHANNEL.registerClientboundDeferred(PlaySoundRecord.class);

		// Server event handler for setting the URL on the Custom Record
		NETWORK_CHANNEL.registerServerbound(SetURLRecord.class, (payload, context) -> {
			PlayerEntity player = context.player();
			ItemStack stack = Arrays.stream(Hand.values()).map(player::getStackInHand).filter(currentStack -> currentStack.getItem() == CUSTOM_RECORD).findFirst().orElse(null);

			if (stack == null) {
				player.sendMessage(Text.literal("VinURL-Disc needed in Hand!"), true);
				return;
			}

			String url;

			try {
				url = new URI(payload.url()).toURL().toString();

			} catch (Exception e) {
				player.sendMessage(Text.literal("Song URL is invalid!"), true);
				return;
			}

			if (url.length() > 400) {
				player.sendMessage(Text.literal("Song URL is too long!"), true);
				return;
			}

			player.playSoundToPlayer(SoundEvents.ENTITY_VILLAGER_WORK_CARTOGRAPHER, SoundCategory.BLOCKS, 1.0f, 1.0f);

			NbtCompound nbt = new NbtCompound();
			nbt.put(URL_KEY, url);
			nbt.put(LOOP_KEY, payload.loop());
			stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
		});
	}
	public record PlaySoundRecord(BlockPos position, String url, Boolean loop) {}

	public record SetURLRecord(String url, Boolean loop) {}

	public record GUIRecord(String url, Boolean loop) {}

}
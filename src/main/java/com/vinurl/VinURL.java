package com.vinurl;

import com.vinurl.items.VinURLDiscItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
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

public class VinURL implements ModInitializer {
	public static final String MOD_ID = "vinurl";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Path VINURLPATH = FabricLoader.getInstance().getGameDir().resolve(MOD_ID);
	public static final Identifier PLACEHOLDER_SOUND_IDENTIFIER = new Identifier(MOD_ID, "placeholder_sound");
	public static final SoundEvent PLACEHOLDER_SOUND = Registry.register(
			Registries.SOUND_EVENT,
			PLACEHOLDER_SOUND_IDENTIFIER,
			SoundEvent.of(PLACEHOLDER_SOUND_IDENTIFIER)
	);
	public static final Item CUSTOM_RECORD = Registry.register(
			Registries.ITEM,
			new Identifier(MOD_ID, "custom_record"),
			new VinURLDiscItem(
					17, PLACEHOLDER_SOUND, new Item.Settings().maxCount(1), 0
			)
	);

	@Override
	public void onInitialize() {
		// Register the Custom Record to the Tools Item Group
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register((content) -> {
			content.add(CUSTOM_RECORD);
		});

		// Server event handler for setting the URL on the Custom Record
		PayloadTypeRegistry.playC2S().register(SetURLPayload.ID, SetURLPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SetURLPayload.ID, (payload, context) -> {
			PlayerEntity player = context.player();
			ItemStack currentItem = player.getStackInHand(player.getActiveHand());

			if (currentItem.getItem() != CUSTOM_RECORD) {
				return;
			}

			String urlName = payload.urlName();

			try {
				new URI(urlName);

			} catch (Exception e) {
				player.sendMessage(Text.literal("Song URL is invalid!"));
				return;
			}

			if (urlName.length() >= 400) {
				player.sendMessage(Text.literal("Song URL is too long!"));
				return;
			}

			player.playSoundToPlayer(SoundEvents.ENTITY_VILLAGER_WORK_CARTOGRAPHER, SoundCategory.BLOCKS, 1.0f, 1.0f);
			NbtCompound currentNbt = new NbtCompound();
			currentNbt.putString("music_url", urlName);
			currentItem.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(currentNbt));
		});
	}

	public record PlaySoundPayload(BlockPos blockPos, String urlName) implements CustomPayload {
		public static final CustomPayload.Id<PlaySoundPayload> ID = CustomPayload.id("vinurl:play_sound");
		public static final PacketCodec<PacketByteBuf, PlaySoundPayload> CODEC = PacketCodec.tuple(PacketCodecs.codec(BlockPos.CODEC), PlaySoundPayload::blockPos, PacketCodecs.STRING, PlaySoundPayload::urlName, PlaySoundPayload::new);

		@Override
		public Id<PlaySoundPayload> getId() {
			return ID;
		}
	}

	public record SetURLPayload(String urlName) implements CustomPayload {
		public static final CustomPayload.Id<SetURLPayload> ID = CustomPayload.id("vinurl:record_set_url");
		public static final PacketCodec<PacketByteBuf, SetURLPayload> CODEC = PacketCodecs.STRING.xmap(SetURLPayload::new, SetURLPayload::urlName).cast();

		@Override
		public Id<SetURLPayload> getId() {
			return ID;
		}
	}


	public record RecordGUIPayload(String urlName) implements CustomPayload {
		public static final CustomPayload.Id<RecordGUIPayload> ID = CustomPayload.id("vinurl:record_gui");
		public static final PacketCodec<PacketByteBuf, RecordGUIPayload> CODEC = PacketCodecs.STRING.xmap(RecordGUIPayload::new, RecordGUIPayload::urlName).cast();


		@Override
		public Id<RecordGUIPayload> getId() {
			return ID;
		}
	}

}
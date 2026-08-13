package com.vinurl;

import com.vinurl.component.AudioComponent;
import com.vinurl.item.URLDisc;
import com.vinurl.net.ServerEvent;
import com.vinurl.net.packet.GUIPacket;
import com.vinurl.net.packet.PlaySoundPacket;
import com.vinurl.net.packet.SetURLPacket;
import com.vinurl.net.packet.StopSoundPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import static com.vinurl.util.Constants.*;

public class VinURL implements ModInitializer {
	public static final DataComponentType<AudioComponent> AUDIO_COMPONENT = Registry.register(
		BuiltInRegistries.DATA_COMPONENT_TYPE,
		AUDIO_COMPONENT_ID,
		DataComponentType.<AudioComponent>builder().persistent(AudioComponent.CODEC).build()
	);

	public static final Item CUSTOM_RECORD = Registry.register(
		BuiltInRegistries.ITEM,
		CUSTOM_RECORD_ID,
		new URLDisc()
	);

	public static final SoundEvent PLACEHOLDER_SOUND = Registry.register(
		BuiltInRegistries.SOUND_EVENT,
		PLACEHOLDER_SOUND_ID,
		SoundEvent.createVariableRangeEvent(PLACEHOLDER_SOUND_ID)
	);

	@Override
	public void onInitialize() {

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register((itemGroup) -> {
			itemGroup.accept(CUSTOM_RECORD);
		});

		PayloadTypeRegistry.playC2S().register(SetURLPacket.TYPE, SetURLPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(GUIPacket.TYPE, GUIPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(PlaySoundPacket.TYPE, PlaySoundPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(StopSoundPacket.TYPE, StopSoundPacket.CODEC);

		ServerEvent.register();
	}
}
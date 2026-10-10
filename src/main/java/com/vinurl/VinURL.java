package com.vinurl;

import com.vinurl.component.DataComponents;
import com.vinurl.item.Items;
import com.vinurl.net.ServerEvent;
import com.vinurl.net.packet.GUIPacket;
import com.vinurl.net.packet.PlaySoundPacket;
import com.vinurl.net.packet.SetURLPacket;
import com.vinurl.net.packet.StopSoundPacket;
import com.vinurl.sound.SoundEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

import static com.vinurl.item.Items.CUSTOM_RECORD;

public class VinURL implements ModInitializer {
	public static final String MOD_ID = "vinurl";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Path VINURLPATH = FabricLoader.getInstance().getGameDir().resolve(MOD_ID);

	public static ResourceLocation identifier(String id) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, id);
	}

	@Override
	public void onInitialize() {

		PayloadTypeRegistry.playC2S().register(SetURLPacket.TYPE, SetURLPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(GUIPacket.TYPE, GUIPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(PlaySoundPacket.TYPE, PlaySoundPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(StopSoundPacket.TYPE, StopSoundPacket.CODEC);

		SoundEvents.register();
		DataComponents.register();
		Items.register();
		ServerEvent.register();

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register((itemGroup) -> {
			itemGroup.accept(CUSTOM_RECORD);
		});
	}
}
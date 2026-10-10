package com.vinurl.component;

import com.mojang.serialization.Codec;
import com.vinurl.VinURL;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import static com.vinurl.VinURL.LOGGER;
import static com.vinurl.VinURL.MOD_ID;

public class DataComponents {

	public static final DataComponentType<AudioComponent> AUDIO_COMPONENT = registerComponent("audio_data", AudioComponent.CODEC);

	private static <T> DataComponentType<T> registerComponent(String id, Codec<T> codec) {
		ResourceLocation identifier = VinURL.identifier(id);
		DataComponentType<T> dataComponentType =  DataComponentType.<T>builder().persistent(codec).build();
		return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, identifier, dataComponentType);
	}

	public static void register() {
		LOGGER.info("Registering " + MOD_ID + " Components");
	}
}

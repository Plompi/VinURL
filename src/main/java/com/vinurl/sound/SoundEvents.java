package com.vinurl.sound;

import com.vinurl.VinURL;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import static com.vinurl.VinURL.LOGGER;
import static com.vinurl.VinURL.MOD_ID;

public class SoundEvents {

	public static final SoundEvent PLACEHOLDER_SOUND = registerSound("placeholder_sound");

	private static SoundEvent registerSound(String id) {
		ResourceLocation identifier = VinURL.identifier(id);
		SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(identifier);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, identifier, soundEvent);
	}

	public static void register() {
		LOGGER.info("Registering " + MOD_ID + " Sounds");
	}
}

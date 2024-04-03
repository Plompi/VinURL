package com.vinurl;

import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.floatprovider.ConstantFloatProvider;

public class FileSound implements SoundInstance {
	private final String fileName;
	private final Vec3d position;

	public FileSound(String fileName, Vec3d position) {
		this.fileName = fileName;
		this.position = position;
	}

	public Identifier getId() {
		return new Identifier(VinURL.MOD_ID, "customsound/" + fileName);
	}

	public WeightedSoundSet getSoundSet(SoundManager soundManager) {
		return new WeightedSoundSet(getId(), null);
	}

	public Sound getSound() {
		return new Sound(getId().toString(), ConstantFloatProvider.create(getVolume()), ConstantFloatProvider.create(getPitch()), 1, Sound.RegistrationType.SOUND_EVENT, true, false, 64);
	}

	public SoundCategory getCategory() {
		return SoundCategory.RECORDS;
	}

	public boolean isRepeatable() {
		return false;
	}

	public boolean isRelative() {
		return false;
	}

	public int getRepeatDelay() {
		return 0;
	}

	public float getVolume() {
		return 1;
	}

	public float getPitch() {
		return 1;
	}

	public double getX() {
		return position.x;
	}

	public double getY() {
		return position.y;
	}

	public double getZ() {
		return position.z;
	}

	public AttenuationType getAttenuationType() {
		return SoundInstance.AttenuationType.LINEAR;
	}
}
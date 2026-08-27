package com.vinurl.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record AudioComponent(String url, int duration, boolean lock) {
	public static final AudioComponent DEFAULT = new AudioComponent("", 0, false);

	public static final Codec<AudioComponent> CODEC = RecordCodecBuilder.create(builder -> {
		return builder.group(
			Codec.STRING.fieldOf("url").forGetter(AudioComponent::url),
			Codec.INT.fieldOf("duration").forGetter(AudioComponent::duration),
			Codec.BOOL.fieldOf("lock").forGetter(AudioComponent::lock)
		).apply(builder, AudioComponent::new);
	});
}

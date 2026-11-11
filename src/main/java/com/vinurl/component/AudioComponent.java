package com.vinurl.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record AudioComponent(String url, int duration, boolean lock) {
	public static final int MAX_URL_LENGTH = 400;
	public static final int MIN_DURATION = 0;
	public static final int MAX_DURATION = 3600;
	public static final AudioComponent DEFAULT = new AudioComponent("", MAX_DURATION / 2, false);

	public static final Codec<AudioComponent> CODEC = RecordCodecBuilder.create(builder -> {
		return builder.group(
			Codec.sizeLimitedString(MAX_URL_LENGTH).fieldOf("url").forGetter(AudioComponent::url),
			Codec.intRange(MIN_DURATION, MAX_DURATION).fieldOf("duration").forGetter(AudioComponent::duration),
			Codec.BOOL.fieldOf("lock").forGetter(AudioComponent::lock)
		).apply(builder, AudioComponent::new);
	});
}

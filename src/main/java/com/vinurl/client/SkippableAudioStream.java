package com.vinurl.client;

import net.minecraft.client.sound.AudioStream;

import java.io.IOException;
import java.nio.ByteBuffer;


public class SkippableAudioStream {
	public static AudioStream createOffsetStream(AudioStream original, long offsetMilliseconds) throws IOException {
		if (offsetMilliseconds <= 0) return original;

		float offsetSeconds = offsetMilliseconds / 1000.0f;
		long bytesToSkip = (long) (offsetSeconds * original.getFormat().getSampleRate() * original.getFormat().getFrameSize());

		while (bytesToSkip > 0) {
			ByteBuffer buffer = original.read((int) Math.min(bytesToSkip, 8192));
			if (buffer == null || buffer.remaining() == 0) break;
			bytesToSkip -= buffer.remaining();
		}

		return original;
	}
}
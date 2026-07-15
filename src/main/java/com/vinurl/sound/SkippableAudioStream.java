package com.vinurl.sound;


import net.minecraft.Util;
import net.minecraft.client.sounds.AudioStream;

import java.io.IOException;
import java.nio.ByteBuffer;


public class SkippableAudioStream {
	private final AudioStream stream;

	public SkippableAudioStream(AudioStream stream) {
		this.stream = stream;
	}

	public AudioStream offset(long offsetMilliseconds) throws IOException {
		for (int i = 0; i < 5 && offsetMilliseconds > 0; i++) {
			offsetMilliseconds = skipBytes(calculateBytes(offsetMilliseconds));
		}
		return stream;
	}

	private long skipBytes(long bytes) throws IOException {
		long start = Util.getMillis();
		while (bytes > 0) {
			ByteBuffer buffer = stream.read((int) Math.min(bytes, 8192));
			if (!buffer.hasRemaining()) {
				break;
			}
			bytes -= buffer.remaining();
		}
		return Util.getMillis() - start;
	}

	private long calculateBytes(long millis) {
		return (long) (millis / 1000.0 * stream.getFormat().getSampleRate() * stream.getFormat().getFrameSize());
	}
}
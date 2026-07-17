package com.vinurl.sound;


import net.minecraft.Util;
import net.minecraft.client.sounds.JOrbisAudioStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;


public class SkippableAudioStream extends JOrbisAudioStream {
	public SkippableAudioStream(InputStream stream, long offsetMilliseconds) throws IOException {
		super(stream);
		this.offset(offsetMilliseconds);
	}

	private void offset(long offsetMilliseconds) throws IOException {
		for (int i = 0; i < 5 && offsetMilliseconds > 0; i++) {
			offsetMilliseconds = skipBytes(calculateBytes(offsetMilliseconds));
		}
	}

	private long skipBytes(long bytes) throws IOException {
		long start = Util.getMillis();
		while (bytes > 0) {
			ByteBuffer buffer = this.read((int) Math.min(bytes, 8192));
			if (!buffer.hasRemaining()) {
				break;
			}
			bytes -= buffer.remaining();
		}
		return Util.getMillis() - start;
	}

	private long calculateBytes(long millis) {
		return (long) (millis / 1000.0 * this.getFormat().getSampleRate() * this.getFormat().getFrameSize());
	}
}
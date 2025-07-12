package com.vinurl.client;

import net.minecraft.client.sound.AudioStream;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.nio.ByteBuffer;


public class SkippableAudioStream implements AudioStream {
	private final AudioStream originalStream;
	private final long bytesToSkip;
	private long bytesSkipped = 0;
	private final AudioFormat format;
	private boolean hasSkipped = false;

	public SkippableAudioStream(AudioStream originalStream, long shiftMilliseconds) {
		this.originalStream = originalStream;
		this.format = originalStream.getFormat();
		this.bytesToSkip = (long) (shiftMilliseconds / 1000.0 * format.getSampleRate() * format.getFrameSize());
	}

	@Override
	public AudioFormat getFormat() {
		return format;
	}

	@Override
	public ByteBuffer read(int size) throws IOException {
		if (!hasSkipped) {
			skipInitialBytes();
			hasSkipped = true;
		}

		return originalStream.read(size);
	}

	private void skipInitialBytes() throws IOException {
		ByteBuffer buffer;
		while (bytesSkipped < bytesToSkip) {
			buffer = originalStream.read(4096);
			if (buffer == null || buffer.remaining() == 0) {
				break;
			}

			int bytesToConsume = (int) Math.min(bytesToSkip - bytesSkipped, buffer.remaining());
			buffer.position(buffer.position() + bytesToConsume);
			bytesSkipped += bytesToConsume;
		}
	}

	@Override
	public void close() throws IOException {
		originalStream.close();
	}
}
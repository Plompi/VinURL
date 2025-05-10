package com.vinurl.exe;

import org.apache.commons.lang3.SystemUtils;

import static com.vinurl.util.Constants.VINURLPATH;

public class FFProbe extends Executable {
	private FFProbe() {
		super("ffprobe",
				VINURLPATH.resolve("ffmpeg").toFile(),
				String.format("ffmpeg-%s-x64.zip", (SystemUtils.IS_OS_LINUX ? "linux" : SystemUtils.IS_OS_MAC ? "osx" : "windows")),
				"Tyrrrz/FFmpegBin");
	}

	public static FFProbe getInstance() {
		return FFmpegHolder.instance;
	}

	private static final class FFmpegHolder {
		private static final FFProbe instance = new FFProbe();
	}
}
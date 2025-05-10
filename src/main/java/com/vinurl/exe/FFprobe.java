package com.vinurl.exe;

import org.apache.commons.lang3.SystemUtils;

import static com.vinurl.util.Constants.VINURLPATH;

public class FFprobe extends Executable {
	private FFprobe() {
		super("ffprobe",
				VINURLPATH.resolve("ffmpeg").toFile(),
				String.format("ffmpeg-%s-x64.zip", (SystemUtils.IS_OS_LINUX ? "linux" : SystemUtils.IS_OS_MAC ? "osx" : "windows")),
				"Tyrrrz/FFmpegBin");
	}

	public static FFprobe getInstance() {
		return FFprobeHolder.instance;
	}

	private static final class FFprobeHolder {
		private static final FFprobe instance = new FFprobe();
	}
}
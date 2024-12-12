package com.vinurl.exe;

import com.vinurl.VinURL;
import org.apache.commons.lang3.SystemUtils;

public class FFmpeg extends Executable {
	public FFmpeg() {
		super("ffmpeg" + (SystemUtils.IS_OS_WINDOWS ? ".exe" : ""),
				VinURL.VINURLPATH.resolve("ffmpeg").toFile(),
				String.format("ffmpeg-%s-x64.zip", (SystemUtils.IS_OS_LINUX ? "linux" : SystemUtils.IS_OS_MAC ? "osx" : "windows")),
				"Tyrrrz/FFmpegBin");
	}

	public static FFmpeg getInstance() {
		return FFmpeg.FFmpegHolder.instance;
	}

	private static final class FFmpegHolder {
		private static final FFmpeg instance = new FFmpeg();
	}
}
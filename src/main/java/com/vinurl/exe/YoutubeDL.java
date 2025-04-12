package com.vinurl.exe;

import org.apache.commons.lang3.SystemUtils;
import static com.vinurl.util.Constants.*;

public class YoutubeDL extends Executable {
	private YoutubeDL() {
		super("yt-dlp" + (SystemUtils.IS_OS_WINDOWS ? ".exe" : ""),
				VINURLPATH.resolve("youtubedl").toFile(),
				String.format("yt-dlp%s", (SystemUtils.IS_OS_LINUX ? "_linux" : SystemUtils.IS_OS_MAC ? "_macos" : ".exe")),
				"yt-dlp/yt-dlp");
	}

	public static YoutubeDL getInstance() {
		return YoutubeDLHolder.instance;
	}

	private static final class YoutubeDLHolder {
		private static final YoutubeDL instance = new YoutubeDL();
	}
}
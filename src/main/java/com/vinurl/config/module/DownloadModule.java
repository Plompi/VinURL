package com.vinurl.config.module;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "audio")
public class DownloadModule implements ConfigData {
	@ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
	@ConfigEntry.Gui.Tooltip public AudioQuality audioBitrate = AudioQuality.MEDIUM;

	public enum AudioQuality {
		LOW("48K"),
		MEDIUM("96K"),
		HIGH("128K");

		private final String value;

		AudioQuality(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	@ConfigEntry.Gui.Tooltip public String parameters = "";
}

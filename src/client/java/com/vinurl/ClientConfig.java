package com.vinurl;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.RangeConstraint;
import io.wispforest.owo.config.annotation.SectionHeader;


@SuppressWarnings("unused")
@Modmenu(modId = VinURL.MOD_ID)
@Config(name = "VinURLConfig", wrapperName = "VinURLConfig")
public class ClientConfig {

	@SectionHeader("General")
	public Boolean UpdateCheckingOnStartup = true;
	@SectionHeader("AudioSettings")
	@RangeConstraint(min = 1, max = 60)
	public byte MaxAudioInMinutes = 60;

	public Choices AudioBitrate = Choices.MEDIUM;

	public enum Choices {
		LOW("48K"),
		MEDIUM("96K"),
		HIGH("128K");

		private final String value;

		Choices(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}
}
package com.vinurl.client;

import io.wispforest.owo.config.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.vinurl.util.Constants.*;


@SuppressWarnings("unused")
@Modmenu(modId = MOD_ID)
@Config(name = "VinURLConfig", wrapperName = "VinURLConfig")
public class ClientConfig {

	@SectionHeader("General")
	public boolean DownloadEnabled = true;
	public boolean UpdateCheckingOnStartup = true;
	public boolean ShowDescription = true;

	@PredicateConstraint("urlSanitization")
	public List<String> urlWhitelist =  new ArrayList<>(List.of("https://www.youtube.com", "https://www.soundcloud.com"));

	public static boolean urlSanitization(List<String> list) {
		for (String s : list) {
			try {
				if (!(new URI(s).toURL().toString().equals(s))) {
					return false;
				}
			} catch (Exception e) {
				return false;
			}
		}
		return true;
	}

	@SectionHeader("AudioSettings")
	@RangeConstraint(min = 1, max = 60)
	public byte MaxAudioInMinutes = 60;

	public AudioQuality AudioBitrate = AudioQuality.MEDIUM;

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
}
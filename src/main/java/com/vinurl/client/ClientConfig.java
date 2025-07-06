package com.vinurl.client;

import io.wispforest.owo.config.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.vinurl.util.Constants.MOD_ID;


@SuppressWarnings("unused")
@Modmenu(modId = MOD_ID)
@Config(name = "VinURLConfig", wrapperName = "VinURLConfig")
public class ClientConfig {

	@SectionHeader("general")
	public boolean downloadEnabled = true;
	public boolean updatesOnStartup = true;
	public boolean showDescription = true;

	@PredicateConstraint("urlSanitization")
	public List<String> urlWhitelist = new ArrayList<>(List.of("https://www.youtube.com", "https://soundcloud.com"));

	public static boolean urlSanitization(List<String> list) {
		return list.stream().allMatch(url -> {
			try {
				return new URI(url).toURL().toString().equals(url);
			} catch (Exception e) {
				return false;
			}
		});
	}

	@SectionHeader("audioSettings")
	public AudioQuality audioBitrate = AudioQuality.MEDIUM;

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
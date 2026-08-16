package com.vinurl.config.module;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

@Config(name = "general")
public class GeneralModule implements ConfigData {
	@ConfigEntry.Gui.Tooltip public boolean downloadEnabled = true;
	@ConfigEntry.Gui.Tooltip public boolean updatesOnStartup = true;
	@ConfigEntry.Gui.Tooltip public boolean showDescription = true;

	public List<String> urlWhitelist = new ArrayList<>();
}

package com.vinurl.config;

import com.vinurl.config.module.DownloadModule;
import com.vinurl.config.module.GeneralModule;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.minecraft.client.gui.screens.Screen;

import static com.vinurl.util.Constants.MOD_ID;


@Config(name = MOD_ID)
public class ClientConfig extends PartitioningSerializer.GlobalData {
	@ConfigEntry.Category("general")
	@ConfigEntry.Gui.TransitiveObject
	public GeneralModule general = new GeneralModule();

	@ConfigEntry.Category("audio")
	@ConfigEntry.Gui.TransitiveObject
	public DownloadModule download = new DownloadModule();

	public static void register() {
		AutoConfig.register(
			ClientConfig.class,
			PartitioningSerializer.wrap(JanksonConfigSerializer::new)
		);
	}

	public static ClientConfig getConfig() {
		return AutoConfig.getConfigHolder(ClientConfig.class).getConfig();
	}

	public static Screen getConfigScreen(Screen parent) {
		return AutoConfig.getConfigScreen(ClientConfig.class, parent).get();
	}
}
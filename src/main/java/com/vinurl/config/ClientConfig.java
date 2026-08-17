package com.vinurl.config;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.ConfigField;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.autogen.*;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.vinurl.util.Constants.MOD_ID;


public class ClientConfig {
	public static ConfigClassHandler<ClientConfig> HANDLER = ConfigClassHandler.createBuilder(ClientConfig.class)
		.id(ResourceLocation.fromNamespaceAndPath(MOD_ID, "config"))
		.serializer(config -> GsonConfigSerializerBuilder.create(config)
			.setPath(FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json5"))
			.setJson5(true)
			.build())
		.build();

	public static ClientConfig getConfig() {
		HANDLER.load();
		return HANDLER.instance();
	}

	public static Screen getConfigScreen(Screen parent) {
		return HANDLER.generateGui().generateScreen(parent);
	}

	@AutoGen(category = "general")
	@TickBox
	@SerialEntry
	public boolean downloadEnabled = true;
	@AutoGen(category = "general")
	@TickBox
	@SerialEntry
	public boolean updatesOnStartup = true;
	@AutoGen(category = "general")
	@TickBox
	@SerialEntry
	public boolean showDescription = true;

	@AutoGen(category = "general")
	@ListGroup(valueFactory = ListFactory.class, controllerFactory = ListFactory.class)
	@SerialEntry
	public List<String> urlWhitelist = new ArrayList<>();

	@AutoGen(category = "download")
	@EnumCycler
	@SerialEntry
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

	@AutoGen(category = "download")
	@StringField
	@SerialEntry
	public String parameters = "";

	public static class ListFactory implements ListGroup.ValueFactory<String>, ListGroup.ControllerFactory<String> {
		@Override
		public String provideNewValue() {
			return "";
		}

		@Override
		public ControllerBuilder<String> createController(ListGroup annotation, ConfigField<List<String>> field, OptionAccess storage, Option<String> option) {
			return StringControllerBuilder.create(option);
		}
	}
}
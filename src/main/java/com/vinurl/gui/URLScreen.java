package com.vinurl.gui;

import static com.vinurl.VinURL.*;
import static com.vinurl.client.VinURLClient.*;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.Component;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class URLScreen extends BaseUIModelScreen<StackLayout> {
	private final String inputDefaultText;
	TextBoxComponent url;
	LabelComponent placeholder;
	Boolean isInitial = true;

	public URLScreen(String inputDefaultText) {
		super(StackLayout.class, DataSource.asset(Identifier.of(MOD_ID, "disc_url_screen")));
		this.inputDefaultText = inputDefaultText;
	}

	@Override
	protected void build(StackLayout stackLayout) {
		placeholder = stackLayout.childById(LabelComponent.class,"placeholder");
		url = stackLayout.childById(TextBoxComponent.class, "url");

		url.keyPress().subscribe((i, i1, i2) -> {
			if (i == GLFW.GLFW_KEY_ESCAPE || i == GLFW.GLFW_KEY_ENTER) {
				if (!url.getText().equals(inputDefaultText)) {
					NETWORK_CHANNEL.clientHandle().send(new SetURLRecord(!isAprilFoolsDay ? url.getText() : "https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
				}
				MinecraftClient.getInstance().setScreen(null);
			}
			return true;
		});

		url.onChanged().subscribe(newText -> placeholder.text(newText.isEmpty() ? Text.literal("URL") : Text.literal("")));
	}

	@Override
	protected void init() {
		super.init();
		if (isInitial){
			url.setText(inputDefaultText);
			isInitial = false;
		}
		url.setFocused(true);
		Objects.requireNonNull(url.focusHandler()).focus(url, Component.FocusSource.KEYBOARD_CYCLE);
		setInitialFocus(url);
		url.setFocusUnlocked(false);
	}
}
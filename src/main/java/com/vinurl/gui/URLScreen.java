package com.vinurl.gui;

import com.vinurl.net.ServerEvent;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.SmallCheckboxComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.PositionedRectangle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

import static com.vinurl.client.VinURLClient.IS_APRIL_FOOLS_DAY;
import static com.vinurl.util.Constants.MOD_ID;
import static com.vinurl.util.Constants.NETWORK_CHANNEL;

public class URLScreen extends BaseUIModelScreen<StackLayout> {
	private TextBoxComponent urlTextbox;
	private LabelComponent placeholderLabel;
	private SmallCheckboxComponent loopCheckbox;
	private final String defaultURL;
	private final boolean defaultLoop;

	public URLScreen(String defaultURL, boolean defaultLoop) {
		super(StackLayout.class, DataSource.asset(Identifier.of(MOD_ID, "disc_url_screen")));
		this.defaultURL = defaultURL;
		this.defaultLoop = defaultLoop;
	}

	@Override
	protected void build(StackLayout stackLayout) {
		placeholderLabel = stackLayout.childById(LabelComponent.class,"placeholder");
		loopCheckbox = stackLayout.childById(SmallCheckboxComponent.class, "loop").checked(defaultLoop);
		urlTextbox = stackLayout.childById(TextBoxComponent.class, "url");

		urlTextbox.onChanged().subscribe(newText -> placeholderLabel.text(newText.isEmpty() ? Text.literal("URL") : Text.literal("")));
		urlTextbox.focusLost().subscribe(() -> stackLayout.childById(TextureComponent.class, "text_field_disabled").visibleArea(PositionedRectangle.of(0,0,110,16)));
		urlTextbox.focusGained().subscribe((focusSource) -> stackLayout.childById(TextureComponent.class, "text_field_disabled").visibleArea(PositionedRectangle.of(0,0,0,0)));
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers){
		if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
			NETWORK_CHANNEL.clientHandle().send(new ServerEvent.SetURLRecord(!IS_APRIL_FOOLS_DAY ? urlTextbox.getText() : "https://www.youtube.com/watch?v=dQw4w9WgXcQ", loopCheckbox.checked()));
			MinecraftClient.getInstance().setScreen(null);
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	protected void init() {
		super.init();

		if (urlTextbox.getText().equals("{{placeholder}}")) {
			Objects.requireNonNull(urlTextbox.focusHandler()).focus(urlTextbox, Component.FocusSource.KEYBOARD_CYCLE);
			urlTextbox.setText(defaultURL);
		}
	}
}
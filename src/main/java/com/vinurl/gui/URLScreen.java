package com.vinurl.gui;

import com.vinurl.util.Networking;
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

import static com.vinurl.client.VinURLClient.isAprilFoolsDay;
import static com.vinurl.util.Constants.MOD_ID;
import static com.vinurl.util.Constants.NETWORK_CHANNEL;

public class URLScreen extends BaseUIModelScreen<StackLayout> {
	private TextBoxComponent url;
	private LabelComponent placeholder;
	private SmallCheckboxComponent checkbox;
	private final String inputDefaultText;
	private final boolean loop;

	public URLScreen(String inputDefaultText, boolean loop) {
		super(StackLayout.class, DataSource.asset(Identifier.of(MOD_ID, "disc_url_screen")));
		this.inputDefaultText = inputDefaultText;
		this.loop = loop;
	}

	@Override
	protected void build(StackLayout stackLayout) {
		placeholder = stackLayout.childById(LabelComponent.class,"placeholder");
		checkbox = stackLayout.childById(SmallCheckboxComponent.class, "loop").checked(loop);
		url = stackLayout.childById(TextBoxComponent.class, "url");

		url.onChanged().subscribe(newText -> placeholder.text(newText.isEmpty() ? Text.literal("URL") : Text.literal("")));
		url.focusLost().subscribe(() -> stackLayout.childById(TextureComponent.class, "text_field_disabled").visibleArea(PositionedRectangle.of(0,0,110,16)));
		url.focusGained().subscribe((focusSource) -> stackLayout.childById(TextureComponent.class, "text_field_disabled").visibleArea(PositionedRectangle.of(0,0,0,0)));
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers){
		if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
			NETWORK_CHANNEL.clientHandle().send(new Networking.SetURLRecord(!isAprilFoolsDay ? url.getText() : "https://www.youtube.com/watch?v=dQw4w9WgXcQ", checkbox.checked()));
			MinecraftClient.getInstance().setScreen(null);
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	protected void init() {
		super.init();

		if (url.getText().equals("{{placeholder}}")) {
			Objects.requireNonNull(url.focusHandler()).focus(url, Component.FocusSource.KEYBOARD_CYCLE);
			url.setText(inputDefaultText);
		}
	}
}
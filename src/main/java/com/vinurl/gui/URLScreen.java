package com.vinurl.gui;

import com.vinurl.net.ServerEvent;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.PositionedRectangle;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

import static com.vinurl.client.VinURLClient.CLIENT;
import static com.vinurl.client.VinURLClient.IS_APRIL_FOOLS_DAY;
import static com.vinurl.util.Constants.MOD_ID;
import static com.vinurl.util.Constants.NETWORK_CHANNEL;

public class URLScreen extends BaseUIModelScreen<StackLayout> {
	private LabelComponent placeholderLabel;
	private TextBoxComponent urlTextbox;
	private ButtonComponent loopButton;
	private ButtonComponent lockButton;
	private final String url;
	private boolean loop;
	private boolean lock;

	public URLScreen(String defaultURL, boolean defaultLoop) {
		super(StackLayout.class, DataSource.asset(Identifier.of(MOD_ID, "disc_url_screen")));
		this.url = defaultURL;
		this.loop = defaultLoop;
	}

	@Override
	protected void build(StackLayout stackLayout) {
		placeholderLabel = stackLayout.childById(LabelComponent.class,"placeholder");
		urlTextbox = stackLayout.childById(TextBoxComponent.class, "url");
		loopButton = stackLayout.childById(ButtonComponent.class, "loop_button");
		lockButton = stackLayout.childById(ButtonComponent.class, "lock_button");

		loopButton.renderer(ButtonComponent.Renderer.texture(Identifier.of(MOD_ID, "textures/gui/loop.png"), loop ? 16: 0,0,32,16));
		loopButton.onPress(button -> {
			loopButton.renderer(ButtonComponent.Renderer.texture(Identifier.of(MOD_ID, "textures/gui/loop.png"), (loop ^= true) ? 16: 0,0,32,16));});

		lockButton.renderer(ButtonComponent.Renderer.texture(Identifier.of(MOD_ID, "textures/gui/lock.png"), lock ? 16: 0,0,32,16));
		lockButton.onPress(button -> {
			lockButton.renderer(ButtonComponent.Renderer.texture(Identifier.of(MOD_ID, "textures/gui/lock.png"), (lock ^= true) ? 16: 0,0,32,16));});

		urlTextbox.onChanged().subscribe(newText -> placeholderLabel.text(newText.isEmpty() ? Text.literal("URL") : Text.literal("")));
		urlTextbox.focusLost().subscribe(() -> stackLayout.childById(TextureComponent.class, "text_field_disabled").visibleArea(PositionedRectangle.of(0,0,110,16)));
		urlTextbox.focusGained().subscribe((focusSource) -> stackLayout.childById(TextureComponent.class, "text_field_disabled").visibleArea(PositionedRectangle.of(0,0,0,0)));
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers){
		if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
			NETWORK_CHANNEL.clientHandle().send(new ServerEvent.SetURLRecord(!IS_APRIL_FOOLS_DAY ? urlTextbox.getText() : "https://www.youtube.com/watch?v=dQw4w9WgXcQ", loop, lock));
			CLIENT.setScreen(null);
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	protected void init() {
		boolean initial = uiAdapter == null;
		super.init();

		if (initial) {
			Objects.requireNonNull(urlTextbox.focusHandler()).focus(urlTextbox, Component.FocusSource.KEYBOARD_CYCLE);
			urlTextbox.setText(url);
		}
	}
}
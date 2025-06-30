package com.vinurl.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vinurl.client.AudioHandler;
import com.vinurl.exe.Executable;
import com.vinurl.net.ServerEvent;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.PositionedRectangle;
import io.wispforest.owo.ui.util.NinePatchTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import static com.vinurl.client.VinURLClient.CLIENT;
import static com.vinurl.client.VinURLClient.IS_APRIL_FOOLS_DAY;
import static com.vinurl.util.Constants.MOD_ID;
import static com.vinurl.util.Constants.NETWORK_CHANNEL;

public class URLScreen extends BaseUIModelScreen<StackLayout> {
	private String url;
	private boolean loop;
	private boolean lock;
	private int duration;

	private final ButtonComponent.Renderer SIMULATE_BUTTON_TEXTURE = (matrices, button, delta) -> {
		RenderSystem.enableDepthTest();
		var texture = button.active ? (button.isHovered() ? Identifier.of(MOD_ID, "simulate_button_hovered") : Identifier.of(MOD_ID, "simulate_button")) : Identifier.of(MOD_ID, "simulate_button_disabled");
		NinePatchTexture.draw(texture, matrices, button.getX(), button.getY(), button.getWidth(), button.getHeight());
	};

	private final ButtonComponent.Renderer LOOP_BUTTON_TEXTURE = (matrices, button, delta) -> {
		RenderSystem.enableDepthTest();
		var texture = loop ? Identifier.of(MOD_ID, "loop_button") : Identifier.of(MOD_ID, "loop_button_disabled");
		NinePatchTexture.draw(texture, matrices, button.getX(), button.getY(), button.getWidth(), button.getHeight());
	};

	private final ButtonComponent.Renderer LOCK_BUTTON_TEXTURE = (matrices, button, delta) -> {
		RenderSystem.enableDepthTest();
		var texture = lock ? Identifier.of(MOD_ID, "lock_button") : Identifier.of(MOD_ID, "lock_button_disabled");
		NinePatchTexture.draw(texture, matrices, button.getX(), button.getY(), button.getWidth(), button.getHeight());
	};

	public URLScreen(String defaultURL, int defaultDuration, boolean defaultLoop) {
		super(StackLayout.class, DataSource.asset(Identifier.of(MOD_ID, "disc_url_screen")));
		this.url = defaultURL;
		this.loop = defaultLoop;
		this.duration = defaultDuration;
	}

	@Override
	protected void build(StackLayout stackLayout) {
		LabelComponent placeholderLabel = stackLayout.childById(LabelComponent.class,"placeholder_label");
		TextBoxComponent urlTextbox = stackLayout.childById(TextBoxComponent.class, "url_textbox");
		SlimSliderComponent durationSlider = stackLayout.childById(SlimSliderComponent.class, "duration_slider");
		ButtonComponent loopButton = stackLayout.childById(ButtonComponent.class, "loop_button");
		ButtonComponent lockButton = stackLayout.childById(ButtonComponent.class, "lock_button");
		ButtonComponent simulateButton = stackLayout.childById(ButtonComponent.class, "simulate_button");

		durationSlider.value(duration);
		durationSlider.tooltipSupplier(slider -> {return Text.literal(slider.intValue() + "s");});
		durationSlider.onChanged().subscribe(newValue -> {duration = (int) newValue;});
		durationSlider.mouseScroll().subscribe((mouseX, mouseY, amount) -> {
			durationSlider.value(Math.clamp(durationSlider.value() + amount, durationSlider.min(), durationSlider.max()));
			return true;
		});

		loopButton.renderer(LOOP_BUTTON_TEXTURE);
		loopButton.onPress(button -> loop = !loop);

		lockButton.renderer(LOCK_BUTTON_TEXTURE);
		lockButton.onPress(button -> lock = !lock);

		simulateButton.renderer(SIMULATE_BUTTON_TEXTURE);
		simulateButton.onPress(button -> {
			button.active(false);
			Executable.YT_DLP.executeCommand(AudioHandler.hashURL(url + System.currentTimeMillis()), url, "--print", "duration", "--no-playlist").subscribe(
					line -> {durationSlider.value(Integer.parseInt(line));},
					error -> {button.active(true);},
					() -> {button.active(true);});
		});

		urlTextbox.onChanged().subscribe(newText -> placeholderLabel.text((url = newText).isEmpty() ? Text.literal("URL") : Text.literal("")));
		urlTextbox.text(url);
		urlTextbox.focusLost().subscribe(() -> stackLayout.childById(TextureComponent.class, "text_field_disabled").visibleArea(PositionedRectangle.of(0,0,110,16)));
		urlTextbox.focusGained().subscribe((focusSource) -> stackLayout.childById(TextureComponent.class, "text_field_disabled").visibleArea(PositionedRectangle.of(0,0,0,0)));
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers){
		if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
			NETWORK_CHANNEL.clientHandle().send(new ServerEvent.SetURLRecord(!IS_APRIL_FOOLS_DAY ? url : "https://www.youtube.com/watch?v=dQw4w9WgXcQ", duration, loop, lock));
			CLIENT.setScreen(null);
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
}
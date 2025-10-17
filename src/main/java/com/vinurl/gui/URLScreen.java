package com.vinurl.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vinurl.client.SoundManager;
import com.vinurl.exe.Executable;
import com.vinurl.net.ServerEvent;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.PositionedRectangle;
import io.wispforest.owo.ui.util.NinePatchTexture;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import static com.vinurl.client.VinURLClient.CLIENT;
import static com.vinurl.util.Constants.*;
import static com.vinurl.util.Constants.LOGGER;

public class URLScreen extends BaseUIModelScreen<StackLayout> {
	private String url;
	private boolean loop;
	private boolean lock;
	private boolean sliderDragged;
	private boolean simulate;
	private int duration;

	private final ButtonComponent.Renderer SIMULATE_BUTTON_TEXTURE = (matrices, button, delta) -> {
		RenderSystem.enableDepthTest();
		Identifier texture = !simulate ? (button.active && button.isHovered() ?
			Identifier.of(MOD_ID, "simulate_button_hovered") :
			Identifier.of(MOD_ID, "simulate_button")) :
			Identifier.of(MOD_ID, "simulate_button_disabled");
		NinePatchTexture.draw(texture, matrices, button.getX(), button.getY(), button.getWidth(), button.getHeight());
	};

	private final ButtonComponent.Renderer LOOP_BUTTON_TEXTURE = (matrices, button, delta) -> {
		RenderSystem.enableDepthTest();
		Identifier texture = loop ?
			Identifier.of(MOD_ID, "loop_button") :
			Identifier.of(MOD_ID, "loop_button_disabled");
		NinePatchTexture.draw(texture, matrices, button.getX(), button.getY(), button.getWidth(), button.getHeight());
	};

	private final ButtonComponent.Renderer LOCK_BUTTON_TEXTURE = (matrices, button, delta) -> {
		RenderSystem.enableDepthTest();
		Identifier texture = lock ?
			Identifier.of(MOD_ID, "lock_button") :
			Identifier.of(MOD_ID, "lock_button_disabled");
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
		LabelComponent placeholderLabel = stackLayout.childById(LabelComponent.class, "placeholder_label");
		TextBoxComponent urlTextbox = stackLayout.childById(TextBoxComponent.class, "url_textbox");
		SlimSliderComponent durationSlider = stackLayout.childById(SlimSliderComponent.class, "duration_slider");
		ButtonComponent loopButton = stackLayout.childById(ButtonComponent.class, "loop_button");
		ButtonComponent lockButton = stackLayout.childById(ButtonComponent.class, "lock_button");
		ButtonComponent simulateButton = stackLayout.childById(ButtonComponent.class, "simulate_button");
		TextureComponent textFieldTexture = stackLayout.childById(TextureComponent.class, "text_field_disabled");

		durationSlider.value(duration);
		durationSlider.tooltipSupplier(slider -> Text.literal(String.format("%02d:%02d", duration / 60, duration % 60)));
		durationSlider.mouseDrag().subscribe((mouseX, mouseY, deltaX, deltaY, button) -> {
			sliderDragged = true;
			lockButton.active = loopButton.active = simulateButton.active = false;
			return true;
		});
		durationSlider.mouseUp().subscribe((mouseX, mouseY, button) -> {
			sliderDragged = false;
			lockButton.active = loopButton.active = simulateButton.active = true;
			return true;
		});
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
			if (simulate) {return;}
			simulate = true;
			button.tooltip(Text.literal("Calculating..."));
			Executable.YT_DLP.executeCommand(
				SoundManager.hashURL(url) + "/duration", url, "--print", "DURATION: %(duration)d", "--no-playlist"
			).subscribe("duration")
				.onOutput(line -> {
					String type = line.substring(0, line.indexOf(':') + 1);
					String message = line.substring(type.length()).trim();

					switch (type) {
						case "DURATION:" -> durationSlider.value(Integer.parseInt(message));
						case "WARNING:" -> LOGGER.warn(message);
						case "ERROR:" -> LOGGER.error(message);
						default -> LOGGER.info(line);
					}
				})
				.onError(error -> {button.tooltip(Text.literal("Automatic Duration")); simulate = false;})
				.onComplete(() -> {button.tooltip(Text.literal("Automatic Duration")); simulate = false;})
			.start();
		});

		urlTextbox.onChanged().subscribe(text -> placeholderLabel.text(Text.literal((url = text).isEmpty() ? "URL" : "")));
		urlTextbox.text(url);
		urlTextbox.focusLost().subscribe(() -> textFieldTexture.visibleArea(PositionedRectangle.of(0, 0, 110, 16)));
		urlTextbox.focusGained().subscribe((source) -> textFieldTexture.visibleArea(PositionedRectangle.of(0, 0, 0, 0)));
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
			NETWORK_CHANNEL.clientHandle().send(new ServerEvent.SetURLRecord(url, duration, loop, lock));
			CLIENT.setScreen(null);
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		if (sliderDragged) {
			context.drawTooltip(
				CLIENT.textRenderer,
				Text.literal(String.format("%02d:%02d", duration / 60, duration % 60)),
				mouseX, mouseY
			);
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
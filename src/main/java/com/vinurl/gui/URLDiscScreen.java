package com.vinurl.gui;

import com.vinurl.client.SoundManager;
import com.vinurl.exe.Executable;
import com.vinurl.net.ServerEvent;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.PositionedRectangle;
import io.wispforest.owo.ui.util.NinePatchTexture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import static com.vinurl.client.VinURLClient.CLIENT;
import static com.vinurl.util.Constants.*;

public class URLDiscScreen extends BaseUIModelScreen<StackLayout> {
	private String url;
	private boolean loop;
	private boolean lock;
	private boolean sliderDragged;
	private boolean simulate;
	private int duration;

	private final ButtonComponent.Renderer SIMULATE_BUTTON_TEXTURE = (matrices, button, delta) -> {
		Identifier texture = !simulate ? (button.active && button.isHovered() ?
			SIMULATE_BUTTON_HOVER_ID :
			SIMULATE_BUTTON_ID) :
			SIMULATE_BUTTON_DISABLED_ID;
		NinePatchTexture.draw(texture, matrices, button.getX(), button.getY(), button.getWidth(), button.getHeight());
	};

	private final ButtonComponent.Renderer LOOP_BUTTON_TEXTURE = (matrices, button, delta) -> {
		Identifier texture = loop ?
			LOOP_BUTTON_ID :
			LOOP_BUTTON_DISABLED_ID;
		NinePatchTexture.draw(texture, matrices, button.getX(), button.getY(), button.getWidth(), button.getHeight());
	};

	private final ButtonComponent.Renderer LOCK_BUTTON_TEXTURE = (matrices, button, delta) -> {
		Identifier texture = lock ?
			LOCK_BUTTON_ID :
			LOCK_BUTTON_DISABLED_ID;
		NinePatchTexture.draw(texture, matrices, button.getX(), button.getY(), button.getWidth(), button.getHeight());
	};

	public URLDiscScreen(String defaultURL, int defaultDuration, boolean defaultLoop) {
		super(StackLayout.class, DataSource.asset(URL_DISC_SCREEN_ID));
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
		durationSlider.tooltipSupplier((slider) -> Component.literal("%02d:%02d".formatted(duration / 60, duration % 60)));
		durationSlider.mouseDown().subscribe((click, doubled) -> {
			sliderDragged = true;
			lockButton.active = loopButton.active = simulateButton.active = false;
			return true;
		});
		durationSlider.mouseUp().subscribe((click) -> {
			sliderDragged = false;
			lockButton.active = loopButton.active = simulateButton.active = true;
			return true;
		});
		durationSlider.onChanged().subscribe((newValue) -> duration = (int) newValue);
		durationSlider.mouseScroll().subscribe((mouseX, mouseY, amount) -> {
			durationSlider.value(Math.clamp(durationSlider.value() + amount, durationSlider.min(), durationSlider.max()));
			return true;
		});

		loopButton.renderer(LOOP_BUTTON_TEXTURE);
		loopButton.onPress((button) -> loop = !loop);

		lockButton.renderer(LOCK_BUTTON_TEXTURE);
		lockButton.onPress((button) -> lock = !lock);

		simulateButton.renderer(SIMULATE_BUTTON_TEXTURE);
		simulateButton.onPress((button) -> {
			if (simulate) {return;}
			simulate = true;
			button.tooltip(Component.translatable("gui.vinurl.button.duration.tooltip.calculating"));
			Executable.YT_DLP.executeCommand(
				SoundManager.getFileName(url) + "/duration", url, "--print", "DURATION: %(duration)d", "--no-playlist"
			).subscribe("duration")
				.onOutput((output) -> {
					String type = output.substring(0, output.indexOf(':') + 1);
					String message = output.substring(type.length()).trim();

					switch (type) {
						case "DURATION:" -> durationSlider.value(Integer.parseInt(message));
						case "WARNING:" -> LOGGER.warn(message);
						case "ERROR:" -> LOGGER.error(message);
						default -> LOGGER.info(output);
					}
				})
				.onError((error) -> {button.tooltip(Component.translatable("gui.vinurl.button.duration.tooltip")); simulate = false;})
				.onComplete(() -> {button.tooltip(Component.translatable("gui.vinurl.button.duration.tooltip")); simulate = false;})
			.start();
		});

		urlTextbox.onChanged().subscribe((text) -> placeholderLabel.text(Component.literal((url = text).isEmpty() ? "URL" : "")));
		urlTextbox.text(url);
		urlTextbox.focusLost().subscribe(() -> textFieldTexture.visibleArea(PositionedRectangle.of(0, 0, 110, 16)));
		urlTextbox.focusGained().subscribe((source) -> textFieldTexture.visibleArea(PositionedRectangle.of(0, 0, 0, 0)));
	}

	@Override
	public boolean keyPressed(KeyEvent input) {
		if (input.key() == GLFW.GLFW_KEY_ESCAPE || input.key() == GLFW.GLFW_KEY_ENTER) {
			NETWORK_CHANNEL.clientHandle().send(new ServerEvent.SetURLRecord(url, duration, loop, lock));
			this.onClose();
			return true;
		}
		return super.keyPressed(input);
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		if (sliderDragged) {
			context.setTooltipForNextFrame(
				CLIENT.font,
				Component.literal("%02d:%02d".formatted(duration / 60, duration % 60)),
				mouseX, mouseY
			);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
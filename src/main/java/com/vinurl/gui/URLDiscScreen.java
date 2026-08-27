package com.vinurl.gui;

import static com.vinurl.net.ServerEvent.MAX_DURATION;
import static com.vinurl.net.ServerEvent.MAX_URL_LENGTH;
import static com.vinurl.util.Constants.SIMULATE_BUTTON_DISABLED_ID;
import static com.vinurl.util.Constants.SIMULATE_BUTTON_HOVER_ID;
import static com.vinurl.util.Constants.SIMULATE_BUTTON_ID;

import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

import com.vinurl.net.packet.SetURLPacket;
import com.vinurl.sound.SoundManager;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.LockIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class URLDiscScreen extends Screen {
	private static final int PADDING = 5;

	private static record InitialData(String url, int duration) {
	}

	private final InitialData initialData;

	private EditBox urlTextbox;
	private LockIconButton lockButton;
	private DurationSliderButton durationSlider;
	private ImageButton simulateButton;

	public URLDiscScreen(String url, int duration) {
		this(new InitialData(url, duration));
	}

	private URLDiscScreen(InitialData data) {
		super(Component.literal("URL Disc Screen"));
		this.initialData = data;
	}

	@Override
	protected void init() {
		Vector2i center = new Vector2i(width / 2, height / 2);

		{
			Vector2i size = new Vector2i(240, 20);
			urlTextbox = new EditBox(
					font,
					center.x - (size.x / 2), center.y,
					size.x, size.y,
					Component.translatable("gui.vinurl.editbox.url.narration"));

			urlTextbox.setMaxLength(MAX_URL_LENGTH);
			urlTextbox.setHint(Component.translatable("gui.vinurl.editbox.url.placeholder"));

			urlTextbox.setValue(initialData.url);

			addRenderableWidget(urlTextbox);
		}

		{
			lockButton = new LockIconButton(
					urlTextbox.getX() + urlTextbox.getWidth() + PADDING, urlTextbox.getY(),
					btn -> lockButton.setLocked(!lockButton.isLocked()));

			lockButton.setTooltip(Tooltip.create(Component.translatable("gui.vinurl.button.lock")));

			addRenderableWidget(lockButton);
		}

		{
			durationSlider = new DurationSliderButton(
					urlTextbox.getX(), urlTextbox.getY() + urlTextbox.getHeight() + PADDING,
					urlTextbox.getWidth(), urlTextbox.getHeight(),
					Component.literal("duration_slider"),
					MAX_DURATION);

			durationSlider.setDuration(initialData.duration);

			addRenderableWidget(durationSlider);
		}

		{
			Tooltip defaultTooltip = Tooltip.create(
					Component.translatable("gui.vinurl.button.duration.tooltip"));

			Tooltip calculatingTooltip = Tooltip.create(
					Component.translatable("gui.vinurl.button.duration.tooltip.calculating"));

			simulateButton = new ImageButton(
					lockButton.getX(), lockButton.getY() + lockButton.getHeight() + PADDING,
					20, 20,
					new WidgetSprites(SIMULATE_BUTTON_ID, SIMULATE_BUTTON_DISABLED_ID, SIMULATE_BUTTON_HOVER_ID),
					btn -> {
						durationSlider.active = false;
						lockButton.active = false;

						simulateButton.active = false;
						simulateButton.setTooltip(calculatingTooltip);

						urlTextbox.active = false;
						urlTextbox.setEditable(false);

						SoundManager.simulateDuration(
								urlTextbox.getValue(),
								durationSlider::setDuration,
								() -> {
									durationSlider.active = true;
									lockButton.active = true;

									simulateButton.active = true;
									simulateButton.setTooltip(defaultTooltip);

									urlTextbox.active = true;
									urlTextbox.setEditable(true);
								});
					});

			simulateButton.setTooltip(defaultTooltip);

			addRenderableWidget(simulateButton);
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
			String url = urlTextbox.getValue();
			int duration = durationSlider.getDuration();
			boolean lock = lockButton.isLocked();

			ClientPlayNetworking.send(new SetURLPacket(url, duration, lock));
			this.onClose();
			return true;
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}

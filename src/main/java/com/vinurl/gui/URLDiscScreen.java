package com.vinurl.gui;

import static com.vinurl.net.ServerEvent.MAX_URL_LENGTH;

import org.joml.Vector2i;
import org.lwjgl.glfw.GLFW;

import com.vinurl.net.packet.SetURLPacket;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.LockIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class URLDiscScreen extends Screen {
	private static final int PADDING = 5;

	private static record InitialData(String url, int duration) {
	}

	private final InitialData initialData;

	private EditBox urlTextbox;
	private LockIconButton lockButton;

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
			Component urlPlaceholder = Component.translatable("gui.vinurl.textfield.placeholder");

			Vector2i size = new Vector2i(240, 20);
			urlTextbox = new EditBox(
					font,
					center.x - (size.x / 2), center.y,
					size.x, size.y,
					// no proper string for the edit box's narration atm
					// so this'll do until then
					urlPlaceholder);

			urlTextbox.setMaxLength(MAX_URL_LENGTH);
			urlTextbox.setHint(urlPlaceholder);

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
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
			String url = urlTextbox.getValue();
			// TODO: implement UI for duration
			int duration = 60;
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

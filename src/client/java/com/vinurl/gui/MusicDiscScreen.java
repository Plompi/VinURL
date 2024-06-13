package com.vinurl.gui;

import com.vinurl.VinURL;
import com.vinurl.VinURLClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class MusicDiscScreen extends Screen {
	private static final Identifier TEXTURE = new Identifier(VinURL.MOD_ID, "textures/gui/record_input.png");
	private static final int BACKGROUND_WIDTH = 176;
	private static final int BACKGROUND_HEIGHT = 44;
	private final String inputDefaultText;
	private final TextFieldWidget textField;
	private int x = (width - BACKGROUND_WIDTH) / 2;
	private int y = (height - BACKGROUND_HEIGHT) / 2;

	public MusicDiscScreen(String inputDefaultText) {
		super(Text.literal("VinURL Screen"));

		this.inputDefaultText = inputDefaultText;
		textField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 98, 12, Text.translatable("container.repair"));
		this.textField.setFocused(true);
		this.textField.setMaxLength(400);
		this.setInitialFocus(this.textField);
		this.textField.setDrawsBackground(false);
		this.textField.setEditableColor(-1);
		this.textField.setFocusUnlocked(false);
		textField.setText(this.inputDefaultText);
	}

	@Override
	protected void init() {
		super.init();
		x = (width - BACKGROUND_WIDTH) / 2;
		y = (height - BACKGROUND_HEIGHT) / 2;
		textField.setPosition(x + 62, y + 18);
		this.setFocused(this.textField);
		this.addSelectableChild(this.textField);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
			if (VinURLClient.isAprilFoolsDay) {
				this.textField.setText("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
			}

			if (!this.textField.getText().equals(this.inputDefaultText)) {
				ClientPlayNetworking.send(new VinURL.SetURLPayload(this.textField.getText()));
			}
			MinecraftClient.getInstance().setScreen(null);
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
		context.drawTexture(TEXTURE, x, y, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
		context.drawTexture(TEXTURE, x + 59, y + 14, 0, BACKGROUND_HEIGHT, 110, 16);
		textField.render(context, mouseX, mouseY, delta);

		if (this.textField.getText().isEmpty()) {
			context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal("URL"), this.textField.getX(), this.textField.getY(), 0xAAAAAA);
		}
	}
}
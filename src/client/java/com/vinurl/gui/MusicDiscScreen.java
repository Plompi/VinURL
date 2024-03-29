package com.vinurl.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vinurl.VinURL;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class MusicDiscScreen extends Screen {
	private static final Identifier TEXTURE = new Identifier(VinURL.MOD_ID, "textures/gui/record_input.png");
	private static final int BACKGROUND_WIDTH = 176;
	private static final int BACKGROUND_HEIGHT = 44;
	private int x;
	private int y;
	private final String inputDefaultText;
	private final TextFieldWidget textField;


	public MusicDiscScreen(String inputDefaultText) {
		super(Text.literal("VinURL Screen"));

		this.inputDefaultText = inputDefaultText;
		textField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, (width - BACKGROUND_WIDTH) / 2 + 62, (height - BACKGROUND_HEIGHT) / 2 + 18, 103, 12, Text.translatable("container.repair"));
		this.textField.setFocused(true);
		this.textField.setMaxLength(200);
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
			if (!this.textField.getText().equals(this.inputDefaultText)) {
				PacketByteBuf bufInfo = PacketByteBufs.create();
				bufInfo.writeString(this.textField.getText());
				ClientPlayNetworking.send(VinURL.CUSTOM_RECORD_SET_URL, bufInfo);
			}
			MinecraftClient.getInstance().setScreen(null);
		}
		if (this.textField.keyPressed(keyCode, scanCode, modifiers) || this.textField.isActive()) {
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		fillGradient(matrices, 0, 0, this.width, this.height, -1072689136, -804253680);
		RenderSystem.setShaderTexture(0, TEXTURE);
		drawTexture(matrices, x, y, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
		drawTexture(matrices, x + 59, y + 14, 0, BACKGROUND_HEIGHT, 110, 16);
		textField.render(matrices, mouseX, mouseY, delta);
	}
}
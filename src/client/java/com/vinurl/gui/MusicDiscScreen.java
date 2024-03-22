package com.vinurl.gui;

import com.vinurl.VinURL;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class MusicDiscScreen extends Screen {
    private static final Identifier TEXTURE = new Identifier(VinURL.MOD_ID, "textures/gui/record_input.png");
    private final static int backgroundWidth = 176;
    private final static int backgroundHeight = 44;
    private int x;
    private int y;
    private final String inputDefaultText;
    private final TextFieldWidget textField;


    public MusicDiscScreen(String inputDefaultText) {
        super(Text.literal("VinURL Screen"));

        this.inputDefaultText = inputDefaultText;
        textField = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, (width-backgroundWidth)/2 + 62, (height-backgroundHeight)/2 + 18, 103, 12, Text.translatable("container.repair"));
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
        x = (width - backgroundWidth) / 2;
        y = (height - backgroundHeight) / 2;
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
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
        context.drawTexture(TEXTURE, x + 59, y + 14, 0, backgroundHeight, 110, 16);
        textField.render(context, mouseX, mouseY, delta);
    }
}
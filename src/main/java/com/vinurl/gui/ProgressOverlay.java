package com.vinurl.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

public class ProgressOverlay {
	private static boolean active;
	private static String info;
	private static int percent;

	public static void set(String progressInfo, int progressPercent) {
		active = true;
		info = progressInfo;
		percent = MathHelper.clamp(progressPercent, 0, 100);
	}

	public static void stop() {active = false;}

	public static void render(DrawContext context) {
		if (!active) {return;}

		MinecraftClient client = MinecraftClient.getInstance();

		Text message = Text.literal(info + " ")
				.append(Text.literal("|".repeat(percent / 5)).formatted(Formatting.GREEN))
				.append(Text.literal("|".repeat(20 - percent / 5)).formatted(Formatting.GRAY))
				.append(" " + percent + "%");

		context.drawTextWithShadow(
				client.textRenderer,
				message,
				(client.getWindow().getScaledWidth() - client.textRenderer.getWidth(message)) / 2,
				client.getWindow().getScaledHeight() - 72,
				0xFFFFFF
		);
	}
}

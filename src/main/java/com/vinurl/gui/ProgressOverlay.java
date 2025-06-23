package com.vinurl.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import static com.vinurl.client.VinURLClient.CLIENT;

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


		Text message = Text.literal(info + " ")
				.append(Text.literal("|".repeat(percent / 5)).formatted(Formatting.GREEN))
				.append(Text.literal("|".repeat(20 - percent / 5)).formatted(Formatting.GRAY))
				.append(" " + percent + "%");

		context.drawTextWithShadow(
				CLIENT.textRenderer,
				message,
				(CLIENT.getWindow().getScaledWidth() - CLIENT.textRenderer.getWidth(message)) / 2,
				CLIENT.getWindow().getScaledHeight() - 72,
				0xFFFFFF
		);
	}
}

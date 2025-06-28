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
	private static int animationStep;
	private static long lastAnimationTime;

	public static void set(String progressInfo, int progressPercent) {
		active = true;
		info = progressInfo;
		percent = MathHelper.clamp(progressPercent, 0, 100);
		animationStep = 0;
	}

	public static void stop() {
		active = false;
	}

	public static void render(DrawContext context) {
		if (!active) return;

		Text status;
		Text progress;

		if (percent < 100) {
			status = Text.literal("Downloading");
			progress = Text.literal(info + " ")
					.append(Text.literal("|".repeat(percent / 5)).formatted(Formatting.GREEN))
					.append(Text.literal("|".repeat(20 - percent / 5)).formatted(Formatting.GRAY));
		}

		else{
			long currentTime = System.currentTimeMillis();
			if (currentTime - lastAnimationTime > 100) {
				animationStep = (animationStep + 1) % 20;
				lastAnimationTime = currentTime;
			}
			status = Text.literal("Transcoding");
			progress = Text.literal(info + " ")
					.append(Text.literal("|".repeat(animationStep)).formatted(Formatting.GRAY))
					.append(Text.literal("|").formatted(Formatting.BLUE))
					.append(Text.literal("|".repeat(19 - animationStep)).formatted(Formatting.GRAY));
		}

		context.drawTextWithShadow(CLIENT.textRenderer, status, (CLIENT.getWindow().getScaledWidth() - CLIENT.textRenderer.getWidth(status)) / 2, CLIENT.getWindow().getScaledHeight() - 82, 0xFFFFFF);
		context.drawTextWithShadow(CLIENT.textRenderer, progress, (CLIENT.getWindow().getScaledWidth() - CLIENT.textRenderer.getWidth(progress)) / 2, CLIENT.getWindow().getScaledHeight() - 72, 0xFFFFFF);
	}
}